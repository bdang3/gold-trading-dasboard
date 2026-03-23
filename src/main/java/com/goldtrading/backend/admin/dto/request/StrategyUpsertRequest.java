package com.goldtrading.backend.admin.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record StrategyUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String nameVi,
        @NotBlank @Size(max = 255) String nameEn,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0") BigDecimal monthlyPrice,
        @NotBlank @Size(max = 50) String riskLevel,
        @NotBlank @Size(max = 500) String supportedTimeframes,
        @NotNull Boolean active
) {}
