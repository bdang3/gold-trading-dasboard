package com.goldtrading.backend.strategies.domain.entity;

import com.goldtrading.backend.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "strategies")
public class Strategy extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String code;
    @Column(name="name_vi", nullable = false) private String nameVi;
    @Column(name="name_en", nullable = false) private String nameEn;
    private String description;
    @Column(name="monthly_price", nullable = false) private BigDecimal monthlyPrice;
    @Column(name="risk_level", nullable = false) private String riskLevel;
    @Column(name="supported_timeframes", nullable = false) private String supportedTimeframes;
    @Column(nullable = false) private Boolean active;
}

