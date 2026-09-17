package com.fxtreasury.valuation.consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxtreasury.common.events.TradeCapturedEvent;
import com.fxtreasury.valuation.repository.ActivePositionRepository;
import com.fxtreasury.valuation.service.MtmValuationService;
import com.fxtreasury.valuation.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

//Consumes fx.trades.raw and triggers initial valuation using the latest cached Redis rate:
@Component
@Slf4j
@RequiredArgsConstructor
public class TradeConsumer {
    private final ObjectMapper objectMapper;
    private final ActivePositionRepository activePositionRepository;
    private final RateCacheService rateCacheService;
    private final MtmValuationService valuationService;
    private final StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = "${app.kafka.topics.trades-raw}")
    public void onTradeCaptured(String rawPayload) {
        try {
            String json = rawPayload;
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = objectMapper.readValue(json, String.class);
            }

            TradeCapturedEvent trade = objectMapper.readValue(json, TradeCapturedEvent.class);

            // IDEMPOTENCY CHECK: Redis atomic SETNX with 24-hour TTL
            String idempotencyKey = "trade:seen:" + trade.tradeId();
            Boolean isFirstTimeSeen = stringRedisTemplate.opsForValue()
                    .setIfAbsent(idempotencyKey, "PROCESSED", Duration.ofHours(24));

            if (Boolean.FALSE.equals(isFirstTimeSeen)) {
                log.warn("Duplicate trade detected by consumer. Skipping processing for: {}", trade.tradeRef());
                return;
            }

            log.info("Successfully registered trade in valuation engine: {}", trade.tradeRef());
            activePositionRepository.save(trade);

            BigDecimal latestRate = rateCacheService.getLatestRate(trade.currencyPair());
            if (latestRate != null) {
                valuationService.evaluatePosition(trade, latestRate);
            }
        } catch (Exception e) {
            log.error("Error processing captured trade: {}", e.getMessage(), e);
            // Rethrow so Spring Kafka's DefaultErrorHandler can retry or route to DLT
            throw new RuntimeException("Triggering Kafka ErrorHandler for recovery", e);
        }
    }
}