package com.fxtreasury.valuation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxtreasury.common.enums.TradeSide;
import com.fxtreasury.common.events.TradeCapturedEvent;
import com.fxtreasury.common.events.ValuationEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/*
    Calculates unrealized P&L taking into account trade side (BUY vs SELL):
    BUY: Unrealized P&L = Notional x (Current Market Rate - Contract Rate)
    SELL: Unrealized P&L = Notional x (Contract Rate - Current Market Rate)
 */

@Service
@Slf4j
public class MtmValuationService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String valuationTopic;

    public MtmValuationService(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.kafka.topics.valuation-events}") String valuationTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.valuationTopic = valuationTopic;
    }

    public void evaluatePosition(TradeCapturedEvent trade, BigDecimal currentMarketRate) {
        BigDecimal rateDelta;
        if (trade.side() == TradeSide.BUY) {
            rateDelta = currentMarketRate.subtract(trade.contractRate());
        } else {
            rateDelta = trade.contractRate().subtract(currentMarketRate);
        }

        BigDecimal unrealizedPnl = trade.notional()
                .multiply(rateDelta)
                .setScale(4, RoundingMode.HALF_UP);

        ValuationEvent event = new ValuationEvent(
                trade.tradeId(),
                trade.tradeRef(),
                trade.currencyPair(),
                trade.contractRate(),
                currentMarketRate,
                unrealizedPnl,
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(valuationTopic, trade.tradeId().toString(), payload);
            log.info("Revalued trade {}: Market Rate={}, Unrealized PnL={}",
                    trade.tradeRef(), currentMarketRate, unrealizedPnl);
        } catch (Exception e) {
            log.error("Failed to publish valuation event for trade {}: {}", trade.tradeRef(), e.getMessage());
        }
    }

}