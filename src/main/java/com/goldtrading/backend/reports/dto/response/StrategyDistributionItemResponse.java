package com.goldtrading.backend.reports.dto.response;

import java.math.BigDecimal;

public record StrategyDistributionItemResponse(
        String strategyCode,
        String strategyName,
        long tradeCount,
        BigDecimal profit,
        double percent
) {}
