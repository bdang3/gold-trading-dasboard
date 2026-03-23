package com.goldtrading.backend.admin.dto.response;

import com.goldtrading.backend.common.PortStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PortResponse(UUID id, String code, String ipAddress, Integer portNumber, String environment,
                           String brokerBinding, PortStatus status, UUID currentMt5AccountId,
                           String note, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
