package com.goldtrading.backend.admin.dto.request;

import java.util.UUID;

public record AdminMt5AccountsQueryRequest(
        int page,
        int pageSize,
        String sortBy,
        String sortOrder,
        String search,
        String status,
        String verificationStatus,
        UUID strategyId,
        String strategyCode,
        String timeframe,
        UUID riskRuleId,
        String riskRuleCode,
        String broker,
        String portStatus,
        UUID userId
) {}
