package com.fxtreasury.common.events;

import com.fxtreasury.common.enums.CurrencyPair;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketQuoteEvent(
     CurrencyPair currencyPair,
     BigDecimal bidRate,
     BigDecimal askRate,
     BigDecimal midRate,
     Instant timestamp
) {}