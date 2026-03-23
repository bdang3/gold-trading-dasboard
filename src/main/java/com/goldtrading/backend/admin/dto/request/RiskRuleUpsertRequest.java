package com.goldtrading.backend.admin.dto.request;

import jakarta.validation.constraints.*;

public record RiskRuleUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @NotBlank String paramsJson,
        @NotNull Boolean active
) {}
