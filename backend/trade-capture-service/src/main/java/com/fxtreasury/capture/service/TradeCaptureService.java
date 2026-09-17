package com.fxtreasury.capture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxtreasury.capture.entity.OutboxEntity;
import com.fxtreasury.capture.entity.TradeEntity;
import com.fxtreasury.capture.repository.OutboxRepository;
import com.fxtreasury.capture.repository.TradeRepository;
import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.enums.TradeSide;
import com.fxtreasury.common.enums.TradeStatus;
import com.fxtreasury.common.enums.TradeType;
import com.fxtreasury.common.events.TradeCapturedEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeCaptureService {
    private final TradeRepository tradeRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    @Transactional
    public TradeEntity captureTrade(CurrencyPair currencyPair, TradeSide side, TradeType tradeType, BigDecimal notional, BigDecimal contractRate, Instant valueDate) {
        UUID tradeId = UUID.randomUUID();
        String tradeRef = "TRD-" + System.currentTimeMillis();
        Instant now = Instant.now();

        TradeEntity trade = new TradeEntity(tradeId, tradeRef, currencyPair, side, tradeType, notional, contractRate, TradeStatus.BOOKED, now, valueDate, now);
        tradeRepository.save(trade);

        TradeCapturedEvent event = new TradeCapturedEvent(tradeId, tradeRef, currencyPair, side, tradeType, notional, contractRate, now, valueDate);
        try{
            String payloadJson = objectMapper.writeValueAsString(event);
            OutboxEntity outbox = new OutboxEntity(
                    UUID.randomUUID(), "TRADE", tradeId.toString(),"TradeCaptureEvent",payloadJson,"PENDING",now
            );
            outboxRepository.save(outbox);
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } return trade;
    }
    @Transactional()
    public List<TradeEntity> findAllTradesSortedByCreation() {
        return tradeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}