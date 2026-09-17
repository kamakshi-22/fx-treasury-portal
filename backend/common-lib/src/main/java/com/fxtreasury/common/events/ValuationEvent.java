package com.fxtreasury.common.events;

import com.fxtreasury.common.enums.CurrencyPair;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ValuationEvent(
        UUID tradeId,
        String tradeRef,
        CurrencyPair currencyPair,
        BigDecimal contractRate,
        BigDecimal marketRate,
        BigDecimal unrealizedPnl,
        Instant valuationTimestamp
) {}