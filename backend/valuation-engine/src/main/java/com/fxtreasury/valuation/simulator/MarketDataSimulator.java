package com.fxtreasury.valuation.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.events.MarketQuoteEvent;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MarketDataSimulator {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String marketQuotesTopic;
    private final Random random = new Random();

    // Baseline market prices
    private final Map<CurrencyPair, BigDecimal> baseRates = new ConcurrentHashMap<>(
            Map.of(CurrencyPair.USD_INR, new BigDecimal("83.2500"),
                    CurrencyPair.EUR_USD, new BigDecimal("1.0850"),
                    CurrencyPair.GBP_USD, new BigDecimal("1.2750"),
                    CurrencyPair.USD_JPY, new BigDecimal("155.4000")));

    public MarketDataSimulator(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, @Value("${app.kafka.topics.market-quotes}") String marketQuotesTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.marketQuotesTopic = marketQuotesTopic;
    }

    // Creates random walk ticks for USD_INR and EUR_USD and publishes them to fx.market.quotes every 1000 ms

    @Scheduled(fixedDelay = 1000)
    public void generatePriceTick() {
        for (CurrencyPair pair : baseRates.keySet()) {
            BigDecimal current = baseRates.get(pair);
            // Random fluctuation between -0.05% and +0.05%
            double deltaPercent = (random.nextDouble() - 0.5) * 0.001;
            BigDecimal delta = current.multiply(BigDecimal.valueOf(deltaPercent));
            BigDecimal updatedMid = current.add(delta).setScale(4, RoundingMode.HALF_UP);
            baseRates.put(pair, updatedMid);
            BigDecimal spread = updatedMid.multiply(new BigDecimal("0.0002"));
            BigDecimal bid = updatedMid.subtract(spread).setScale(4, RoundingMode.HALF_UP);
            BigDecimal ask = updatedMid.add(spread).setScale(4, RoundingMode.HALF_UP);
            MarketQuoteEvent quote = new MarketQuoteEvent(pair, bid, ask, updatedMid, Instant.now());
            try {
                String payload = objectMapper.writeValueAsString(quote);
                kafkaTemplate.send(marketQuotesTopic, pair.name(), payload);
            } catch (Exception e) {
                log.error("Failed to publish synthetic market quote: {}", e.getMessage());
            }
        }
    }
}