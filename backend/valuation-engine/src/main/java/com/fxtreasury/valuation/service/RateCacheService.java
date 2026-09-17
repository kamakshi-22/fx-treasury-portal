package com.fxtreasury.valuation.service;



import com.fxtreasury.common.enums.CurrencyPair;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class RateCacheService {
    private static final String KEY_PREFIX = "fx:rate:";
    private final StringRedisTemplate redisTemplate;

    // Stores and retrieves the latest mid-market exchange rate in Redis under fx:rate:{currencyPair}

    public void updateRate(CurrencyPair pair, BigDecimal midRate) {
        redisTemplate.opsForValue().set(KEY_PREFIX + pair.name(), midRate.toPlainString());
    }

    public BigDecimal getLatestRate(CurrencyPair pair) {
        String rate = redisTemplate.opsForValue().get(KEY_PREFIX + pair.name());
        return rate != null ? new BigDecimal(rate) : null;
    }

}