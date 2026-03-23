package com.goldtrading.backend.auth.controller;

import com.goldtrading.backend.auth.dto.request.*;
import com.goldtrading.backend.auth.service.AuthService;
import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.security.ratelimit.InMemoryRateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final InMemoryRateLimiter rateLimiter;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody @Valid RegisterRequest req, @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        if (!rateLimiter.allow("register:" + (ip == null ? "local" : ip))) return ApiResponse.error("RATE_LIMITED", "Too many requests", null);
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody @Valid LoginRequest req, @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        if (!rateLimiter.allow("login:" + (ip == null ? "local" : ip))) return ApiResponse.error("RATE_LIMITED", "Too many requests", null);
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<?> refresh(@RequestBody @Valid RefreshRequest req) { return ApiResponse.ok(authService.refresh(req)); }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody @Valid RefreshRequest req) { authService.logout(req.refreshToken()); return ApiResponse.ok("ok"); }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        String token = authService.forgotPassword(req);
        return ApiResponse.ok(Map.of("message", "If account exists, reset instructions were generated", "demoResetToken", token));
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody @Valid ResetPasswordRequest req) { authService.resetPassword(req); return ApiResponse.ok("ok"); }

    @GetMapping("/me")
    public ApiResponse<?> me(Principal principal) { return ApiResponse.ok(authService.me(principal.getName())); }

    @PostMapping("/change-password")
    public ApiResponse<?> changePassword(Principal principal, @RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(principal.getName(), req);
        return ApiResponse.ok("ok");
    }
}
