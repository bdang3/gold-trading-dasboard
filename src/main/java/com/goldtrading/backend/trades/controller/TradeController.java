package com.goldtrading.backend.trades.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {
    private final ReportsService reportsService;

    @GetMapping("/my")
    public ApiResponse<?> my(Principal principal,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int pageSize,
                             @RequestParam(defaultValue = "openedAt") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortOrder) {
        return ApiResponse.ok(reportsService.trades(principal.getName(), page, pageSize, sortBy, sortOrder));
    }
}
