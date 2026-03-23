package com.goldtrading.backend.admin.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StrategyResponse(UUID id, String code, String nameVi, String nameEn, String description,
                               BigDecimal monthlyPrice, String riskLevel, String supportedTimeframes,
                               Boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
