package com.goldtrading.backend.users.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 255) String fullName,
        String phone,
        String address,
        String preferredLanguage
) {}

