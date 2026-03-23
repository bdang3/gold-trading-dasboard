package com.goldtrading.backend.admin.dto.response;

public record AdminDashboardSummaryResponse(
        long totalUsers,
        long totalMt5Accounts,
        long pendingAccounts,
        long processingAccounts,
        long stoppedAccounts,
        long failedAccounts,
        long availablePorts,
        long occupiedPorts,
        long disabledPorts,
        long recentAlertsCount
) {}
