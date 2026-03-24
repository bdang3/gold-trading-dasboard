package com.goldtrading.backend.trades.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {
    private final ReportsService reportsService;

    @GetMapping("/my")
    public ApiResponse<?> my(Principal principal,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                             @RequestParam(name = "sortBy", defaultValue = "openedAt") String sortBy,
                             @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
                             @RequestParam(name = "account", required = false) String account,
                             @RequestParam(name = "accountNumber", required = false) String accountNumber,
                             @RequestParam(name = "from", required = false) LocalDateTime from,
                             @RequestParam(name = "to", required = false) LocalDateTime to) {
        return ApiResponse.ok(reportsService.trades(principal.getName(), page, pageSize, sortBy, sortOrder, account, accountNumber, from, to));
    }
}
