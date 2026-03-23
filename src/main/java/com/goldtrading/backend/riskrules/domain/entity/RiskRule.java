package com.goldtrading.backend.riskrules.domain.entity;

import com.goldtrading.backend.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "risk_rules")
public class RiskRule extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
    private String description;
    @Column(name="params_json", nullable = false, columnDefinition = "text") private String paramsJson;
    @Column(nullable = false) private Boolean active;
}

