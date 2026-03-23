package com.goldtrading.backend.brokers.service;

import com.goldtrading.backend.brokers.domain.entity.BrokerServer;
import com.goldtrading.backend.brokers.repository.BrokerRepository;
import com.goldtrading.backend.brokers.repository.BrokerServerRepository;
import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.mt5accounts.dto.BrokerCatalogItemResponse;
import com.goldtrading.backend.mt5accounts.dto.BrokerServerCatalogItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrokerCatalogService {
    private final BrokerRepository brokerRepository;
    private final BrokerServerRepository brokerServerRepository;

    public List<BrokerCatalogItemResponse> listActiveBrokers() {
        return brokerRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(broker -> new BrokerCatalogItemResponse(broker.getId(), broker.getCode(), broker.getName()))
                .toList();
    }

    public List<BrokerServerCatalogItemResponse> listActiveServers(UUID brokerId) {
        return brokerServerRepository.findByBrokerIdAndActiveTrueOrderByNameAsc(brokerId).stream()
                .map(server -> new BrokerServerCatalogItemResponse(server.getId(), server.getBroker().getId(), server.getCode(), server.getName()))
                .toList();
    }

    public BrokerServer resolveActiveServer(UUID brokerId, UUID serverId) {
        BrokerServer server = brokerServerRepository.findById(serverId)
                .orElseThrow(() -> new BusinessException("VALIDATION_ERROR", "Server không tồn tại"));
        if (!Boolean.TRUE.equals(server.getActive())) {
            throw new BusinessException("VALIDATION_ERROR", "Server đã bị vô hiệu hóa");
        }
        if (!server.getBroker().getId().equals(brokerId)) {
            throw new BusinessException("VALIDATION_ERROR", "Server không thuộc broker đã chọn");
        }
        if (!Boolean.TRUE.equals(server.getBroker().getActive())) {
            throw new BusinessException("VALIDATION_ERROR", "Broker đã bị vô hiệu hóa");
        }
        return server;
    }
}
