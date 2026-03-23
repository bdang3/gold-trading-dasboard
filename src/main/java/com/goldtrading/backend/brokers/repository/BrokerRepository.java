package com.goldtrading.backend.brokers.repository;

import com.goldtrading.backend.brokers.domain.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BrokerRepository extends JpaRepository<Broker, UUID> {
    List<Broker> findByActiveTrueOrderByNameAsc();
}
