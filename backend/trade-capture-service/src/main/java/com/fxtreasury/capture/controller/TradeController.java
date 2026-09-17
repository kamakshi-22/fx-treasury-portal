package com.fxtreasury.capture.controller;

import com.fxtreasury.capture.entity.TradeEntity;
import com.fxtreasury.capture.service.TradeCaptureService;
import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.enums.TradeSide;
import com.fxtreasury.common.enums.TradeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeCaptureService tradeCaptureService;

    public record TradeRequest(
            @NotNull CurrencyPair currencyPair,
            @NotNull TradeSide side,
            @NotNull TradeType tradeType,
            @NotNull @Positive BigDecimal notional,
            @NotNull @Positive BigDecimal contractRate,
            @NotNull Instant valueDate
    ){}

    //@CrossOrigin(origins = "http://localhost:5173")
    @PostMapping
    public ResponseEntity<TradeEntity> createTrade(@Valid @RequestBody TradeRequest req) {
        TradeEntity trade = tradeCaptureService.captureTrade(
                req.currencyPair,req.side,req.tradeType,req.notional,req.contractRate,req.valueDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(trade);
    }

    //@CrossOrigin(origins = "http://localhost:5173")
    @GetMapping
    public ResponseEntity<List<TradeEntity>> getAllTrades() {
        return ResponseEntity.ok(tradeCaptureService.findAllTradesSortedByCreation());
    }
}