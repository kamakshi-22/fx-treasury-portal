package com.fxtreasury.common.events;

import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.enums.TradeSide;
import com.fxtreasury.common.enums.TradeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeCapturedEvent(
        UUID tradeId,
        String tradeRef,
        CurrencyPair currencyPair,
        TradeSide side,
        TradeType tradeType,
        BigDecimal notional,
        BigDecimal contractRate,
        Instant tradeDate,
        Instant valueDate
) {}