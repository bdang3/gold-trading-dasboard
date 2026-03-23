package com.goldtrading.backend.dashboard.service;

import com.goldtrading.backend.admin.dto.response.AdminDashboardSummaryResponse;
import com.goldtrading.backend.common.AccountStatus;
import com.goldtrading.backend.common.PortStatus;
import com.goldtrading.backend.mt5accounts.repository.MT5AccountRepository;
import com.goldtrading.backend.ports.repository.PortMasterRepository;
import com.goldtrading.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final MT5AccountRepository mt5AccountRepository;
    private final PortMasterRepository portMasterRepository;

    public AdminDashboardSummaryResponse summary() {
        var accounts = mt5AccountRepository.findAll();
        var ports = portMasterRepository.findAll();
        return new AdminDashboardSummaryResponse(
                userRepository.count(),
                accounts.size(),
                accounts.stream().filter(a -> a.getStatus() == AccountStatus.PENDING).count(),
                accounts.stream().filter(a -> a.getStatus() == AccountStatus.PROCESSING).count(),
                accounts.stream().filter(a -> a.getStatus() == AccountStatus.STOPPED).count(),
                accounts.stream().filter(a -> a.getStatus() == AccountStatus.FAILED).count(),
                ports.stream().filter(p -> p.getStatus() == PortStatus.AVAILABLE).count(),
                ports.stream().filter(p -> p.getStatus() == PortStatus.OCCUPIED).count(),
                ports.stream().filter(p -> p.getStatus() == PortStatus.DISABLED).count(),
                0L
        );
    }
}

