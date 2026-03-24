package com.goldtrading.backend.reports.dto.response;

import java.math.BigDecimal;

public record MonthlyProfitItemResponse(String month, BigDecimal profit) {}
