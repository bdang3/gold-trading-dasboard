package com.goldtrading.backend.auth.dto.response;

public record AuthTokensResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}

