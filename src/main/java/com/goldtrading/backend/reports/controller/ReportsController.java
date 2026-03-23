package com.goldtrading.backend.reports.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/my/summary")
    public ApiResponse<?> summary(Principal principal) { return ApiResponse.ok(reportsService.summary(principal.getName())); }

    @GetMapping("/my/trades")
    public ApiResponse<?> trades(Principal principal,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int pageSize,
                                 @RequestParam(defaultValue = "openedAt") String sortBy,
                                 @RequestParam(defaultValue = "desc") String sortOrder) {
        return ApiResponse.ok(reportsService.trades(principal.getName(), page, pageSize, sortBy, sortOrder));
    }
}
