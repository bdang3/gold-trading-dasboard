package com.goldtrading.backend.admin.dto.request;

import com.goldtrading.backend.common.PortStatus;
import jakarta.validation.constraints.*;

public record PortUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String ipAddress,
        @NotNull @Min(1) @Max(65535) Integer portNumber,
        @NotBlank @Size(max = 50) String environment,
        @Size(max = 100) String brokerBinding,
        @NotNull PortStatus status,
        @Size(max = 1000) String note
) {}
