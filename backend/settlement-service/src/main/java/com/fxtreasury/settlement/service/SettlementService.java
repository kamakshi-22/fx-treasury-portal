package com.fxtreasury.settlement.service;

import com.fxtreasury.common.enums.CurrencyPair;
import com.fxtreasury.common.enums.TradeSide;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SettlementService {

    private final JdbcTemplate jdbcTemplate;

    public SettlementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record NettingSummary(
            CurrencyPair currencyPair,
            BigDecimal netAmount,
            String settlementAction,
            List<String> includedTradeRefs
    ) {}

    public List<NettingSummary> executeBilateralNetting() {
        // Query booked trades from fx_object schema
        String sql = "SELECT trade_ref, currency_pair, side, notional FROM fx_object.trades WHERE status = 'BOOKED'";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<CurrencyPair, BigDecimal> netBalances = new HashMap<>();
        Map<CurrencyPair, List<String>> tradeMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            CurrencyPair pair = CurrencyPair.valueOf((String) row.get("currency_pair"));
            TradeSide side = TradeSide.valueOf((String) row.get("side"));
            BigDecimal notional = (BigDecimal) row.get("notional");
            String ref = (String) row.get("trade_ref");

            netBalances.putIfAbsent(pair, BigDecimal.ZERO);
            tradeMap.putIfAbsent(pair, new ArrayList<>());

            // BUY adds to inventory, SELL reduces
            if (side == TradeSide.BUY) {
                netBalances.put(pair, netBalances.get(pair).add(notional));
            } else {
                netBalances.put(pair, netBalances.get(pair).subtract(notional));
            }
            tradeMap.get(pair).add(ref);
        }

        List<NettingSummary> summaries = new ArrayList<>();
        for (Map.Entry<CurrencyPair, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal bal = entry.getValue();
            String action = bal.compareTo(BigDecimal.ZERO) >= 0 ? "RECEIVE" : "PAY";
            summaries.add(new NettingSummary(entry.getKey(), bal.abs(), action, tradeMap.get(entry.getKey())));
        }

        return summaries;
    }

    public String generateMt101(NettingSummary netting) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String txnRef = "NET" + System.currentTimeMillis();

        return String.format(
                """
                :20:%s
                :21R:FX-SETTLE-%s
                :28D:1/1
                :30:%s
                :32B:%s%s
                :50A:/CORP-TREASURY-01
                :59:/CUSTODIAN-BANK-ACC-9988
                :71A:SHA
                -}{5:}{MAC:00000000}{CHK:123456789ABC}
                """,
                txnRef,
                netting.currencyPair().name(),
                today,
                netting.currencyPair().name().substring(0, 3), // Base currency code
                netting.netAmount().toPlainString()
        ).stripIndent();
    }
}