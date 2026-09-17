package com.fxtreasury.valuation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxtreasury.common.events.MarketQuoteEvent;
import com.fxtreasury.common.events.TradeCapturedEvent;
import com.fxtreasury.valuation.repository.ActivePositionRepository;
import com.fxtreasury.valuation.service.MtmValuationService;
import com.fxtreasury.valuation.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class MarketQuoteConsumer {
    private final ObjectMapper objectMapper;
    private final RateCacheService rateCacheService;
    private final ActivePositionRepository activePositionRepository;
    private final MtmValuationService valuationService;

    @KafkaListener(topics = "${app.kafka.topics.market-quotes}")
    public void onMarketQuote(String message) {
        try {
            MarketQuoteEvent quote = objectMapper.readValue(message, MarketQuoteEvent.class);

            // 1. Update Redis rate cache
            rateCacheService.updateRate(quote.currencyPair(), quote.midRate());

            // 2. Re-evaluate open positions
            Collection<TradeCapturedEvent> openTrades = activePositionRepository.findByCurrencyPair(quote.currencyPair());
            for (TradeCapturedEvent trade : openTrades) {
                valuationService.evaluatePosition(trade, quote.midRate());
            }
        } catch (Exception e) {
            log.error("Error processing market quote: {}", e.getMessage());
        }
    }
}