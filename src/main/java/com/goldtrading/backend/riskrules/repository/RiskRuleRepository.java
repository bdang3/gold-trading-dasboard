package com.goldtrading.backend.riskrules.repository;

import com.goldtrading.backend.riskrules.domain.entity.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RiskRuleRepository extends JpaRepository<RiskRule, UUID> {
    List<RiskRule> findByIdIn(Collection<UUID> ids);
    java.util.Optional<RiskRule> findByCodeIgnoreCase(String code);
    List<RiskRule> findByActiveTrueOrderByNameAsc();
}

