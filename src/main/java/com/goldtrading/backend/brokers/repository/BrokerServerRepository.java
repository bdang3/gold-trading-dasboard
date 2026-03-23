package com.goldtrading.backend.brokers.repository;

import com.goldtrading.backend.brokers.domain.entity.BrokerServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BrokerServerRepository extends JpaRepository<BrokerServer, UUID> {
    List<BrokerServer> findByBrokerIdAndActiveTrueOrderByNameAsc(UUID brokerId);
}
