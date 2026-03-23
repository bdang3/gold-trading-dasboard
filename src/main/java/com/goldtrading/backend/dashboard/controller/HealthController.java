package com.goldtrading.backend.dashboard.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<?> health() { return ApiResponse.ok("ok"); }

    @GetMapping("/ready")
    public ApiResponse<?> ready() { return ApiResponse.ok("ready"); }
}

