package com.goldtrading.backend.mt5accounts.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.mt5accounts.dto.*;
import com.goldtrading.backend.mt5accounts.service.Mt5AccountService;
import com.goldtrading.backend.brokers.service.BrokerCatalogService;
import com.goldtrading.backend.riskrules.repository.RiskRuleRepository;
import com.goldtrading.backend.strategies.repository.StrategyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class Mt5AccountController {
    private final Mt5AccountService service;
    private final BrokerCatalogService brokerCatalogService;
    private final StrategyRepository strategyRepository;
    private final RiskRuleRepository riskRuleRepository;

    @GetMapping("/api/v1/mt5-accounts/my")
    public ApiResponse<?> my(Principal principal) { return ApiResponse.ok(service.myAccounts(principal.getName())); }

    @PostMapping("/api/v1/mt5-accounts")
    public ApiResponse<?> create(Principal principal, @RequestBody @Valid CreateMt5AccountRequest req) { return ApiResponse.ok(service.create(principal.getName(), req)); }

    @GetMapping("/api/v1/mt5-accounts/{id}")
    public ApiResponse<?> get(Principal principal, @PathVariable UUID id) { return ApiResponse.ok(service.getMyAccount(principal.getName(), id)); }

    @PatchMapping("/api/v1/mt5-accounts/{id}")
    public ApiResponse<?> patch(Principal principal, @PathVariable UUID id, @RequestBody UpdateMt5AccountRequest req) { return ApiResponse.ok(service.updateMyAccount(principal.getName(), id, req)); }

    @DeleteMapping("/api/v1/mt5-accounts/{id}")
    public ApiResponse<?> delete(Principal principal, @PathVariable UUID id) { service.deleteMyAccount(principal.getName(), id); return ApiResponse.ok("ok"); }

    @PostMapping("/api/v1/mt5-accounts/{id}/verify")
    public ApiResponse<?> verify(Principal principal, @PathVariable UUID id) { return ApiResponse.ok(service.verifyMyAccount(principal.getName(), id)); }

    @PostMapping("/api/v1/mt5-accounts/{id}/submit")
    public ApiResponse<?> submit(Principal principal, @PathVariable UUID id) { return ApiResponse.ok(service.submitMyAccount(principal.getName(), id)); }

    @PostMapping("/api/v1/mt5-accounts/{id}/stop")
    public ApiResponse<?> stop(Principal principal, @PathVariable UUID id) { return ApiResponse.ok(service.stopByUser(principal.getName(), id)); }

    @PostMapping("/api/v1/mt5-accounts/{id}/reconfigure")
    public ApiResponse<?> reconfigure(Principal principal, @PathVariable UUID id, @RequestBody UpdateMt5AccountRequest req) { return ApiResponse.ok(service.reconfigureByUser(principal.getName(), id, req)); }

    @GetMapping("/api/v1/mt5-accounts/catalog/strategies")
    public ApiResponse<?> strategiesCatalog() {
        var data = strategyRepository.findByActiveTrueOrderByNameViAsc().stream()
                .map(s -> new com.goldtrading.backend.admin.dto.response.StrategyResponse(
                        s.getId(), s.getCode(), s.getNameVi(), s.getNameEn(), s.getDescription(),
                        s.getMonthlyPrice(), s.getRiskLevel(), s.getSupportedTimeframes(), s.getActive(), s.getCreatedAt(), s.getUpdatedAt()
                ))
                .toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/api/v1/mt5-accounts/catalog/risk-rules")
    public ApiResponse<?> riskRulesCatalog() {
        var data = riskRuleRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(r -> new com.goldtrading.backend.admin.dto.response.RiskRuleResponse(
                        r.getId(), r.getCode(), r.getName(), r.getDescription(), r.getParamsJson(), r.getActive(), r.getCreatedAt(), r.getUpdatedAt()
                ))
                .toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/api/v1/mt5-accounts/catalog/brokers")
    public ApiResponse<?> brokersCatalog() {
        return ApiResponse.ok(brokerCatalogService.listActiveBrokers());
    }

    @GetMapping("/api/v1/mt5-accounts/catalog/brokers/{brokerId}/servers")
    public ApiResponse<?> brokerServersCatalog(@PathVariable UUID brokerId) {
        return ApiResponse.ok(brokerCatalogService.listActiveServers(brokerId));
    }
}

