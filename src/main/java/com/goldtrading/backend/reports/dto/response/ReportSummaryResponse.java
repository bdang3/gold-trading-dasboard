package com.goldtrading.backend.reports.dto.response;

import java.math.BigDecimal;

public record ReportSummaryResponse(long totalTrades, double winRate, BigDecimal totalProfit, int accounts) {}
