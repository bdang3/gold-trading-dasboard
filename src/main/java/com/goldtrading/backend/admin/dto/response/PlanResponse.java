package com.goldtrading.backend.admin.dto.response;

import com.goldtrading.backend.common.BillingCycle;
import com.goldtrading.backend.common.PlanStatus;
import com.goldtrading.backend.common.PlanType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanResponse(UUID id, String code, PlanType type, String name, BillingCycle billingCycle,
                           BigDecimal price, Integer profitSharePercent, PlanStatus status,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
