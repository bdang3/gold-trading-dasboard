package com.goldtrading.backend.admin.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RiskRuleResponse(UUID id, String code, String name, String description,
                               String paramsJson, Boolean active,
                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
