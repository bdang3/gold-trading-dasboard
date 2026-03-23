package com.goldtrading.backend.admin.dto.request;

import com.goldtrading.backend.common.BillingCycle;
import com.goldtrading.backend.common.PlanStatus;
import com.goldtrading.backend.common.PlanType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PlanUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        @NotNull PlanType type,
        @NotBlank @Size(max = 255) String name,
        @NotNull BillingCycle billingCycle,
        @NotNull @DecimalMin("0") BigDecimal price,
        @NotNull @Min(0) @Max(100) Integer profitSharePercent,
        @NotNull PlanStatus status
) {}
