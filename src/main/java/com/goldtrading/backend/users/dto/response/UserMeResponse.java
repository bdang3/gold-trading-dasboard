package com.goldtrading.backend.users.dto.response;

import com.goldtrading.backend.common.RoleType;
import com.goldtrading.backend.common.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserMeResponse(UUID id, String fullName, String email, String phone, String address, RoleType role,
                             UserStatus status, String preferredLanguage, OffsetDateTime createdAt) {}

