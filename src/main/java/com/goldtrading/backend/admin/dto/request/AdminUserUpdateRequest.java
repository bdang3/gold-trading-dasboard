package com.goldtrading.backend.admin.dto.request;

import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @Size(min = 2, max = 255) String fullName,
        String phone,
        String address,
        String preferredLanguage
) {}
