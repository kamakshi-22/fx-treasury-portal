package com.fxtreasury.valuation.repository;

import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.events.TradeCapturedEvent;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Keeps open trade positions partitioned in-memory for microsecond lookup when incoming quote ticks occur

@Repository
public class ActivePositionRepository {
    // CurrencyPair -> (TradeId -> TradeCapturedEvent)
    // ConcurrentHashMap - thread safe
    private final Map<CurrencyPair, Map<UUID, TradeCapturedEvent>> positionsByPair = new ConcurrentHashMap<>();

    public void save(TradeCapturedEvent trade) {
        positionsByPair
                .computeIfAbsent(trade.currencyPair(), k -> new ConcurrentHashMap<>())
                .put(trade.tradeId(), trade);
    }

    public Collection<TradeCapturedEvent> findByCurrencyPair(CurrencyPair pair) {
        Map<UUID, TradeCapturedEvent> pairTrades = positionsByPair.get(pair);
        return pairTrades != null ? pairTrades.values() : Collections.emptyList();
    }
}