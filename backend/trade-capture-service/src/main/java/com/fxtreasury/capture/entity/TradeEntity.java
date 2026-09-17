package com.fxtreasury.capture.entity;

import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.enums.TradeSide;
import com.fxtreasury.common.enums.TradeStatus;
import com.fxtreasury.common.enums.TradeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
    @Id
    private UUID id;
    @Column(name = "trade_ref", nullable = false, unique = true)
    private String tradeRef;
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_pair", nullable = false)
    private CurrencyPair currencyPair;
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private TradeSide side;
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false)
    private TradeType tradeType;
    @Column(name = "notional", nullable = false, precision = 19, scale = 4)
    private BigDecimal notional;
    @Column(name = "contractRate", nullable = false, precision = 19, scale = 6)
    private BigDecimal contractRate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeStatus status;
    @Column(name = "trade_date", nullable = false)
    private Instant tradeDate;
    @Column(name = "value_date", nullable = false)
    private Instant valueDate;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}