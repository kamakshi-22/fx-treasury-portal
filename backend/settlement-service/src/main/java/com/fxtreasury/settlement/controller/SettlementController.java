package com.fxtreasury.settlement.controller;

import com.fxtreasury.settlement.service.SettlementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlement")
@CrossOrigin(origins = "http://localhost:5173")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/netting-run")
    public ResponseEntity<List<SettlementService.NettingSummary>> runNetting() {
        return ResponseEntity.ok(settlementService.executeBilateralNetting());
    }

    @PostMapping("/export-mt101")
    public ResponseEntity<String> exportMt101(@RequestBody SettlementService.NettingSummary netting) {
        String mt101Content = settlementService.generateMt101(netting);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MT101_" + netting.currencyPair() + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(mt101Content);
    }
}