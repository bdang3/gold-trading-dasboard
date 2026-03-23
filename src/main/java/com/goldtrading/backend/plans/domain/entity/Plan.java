package com.goldtrading.backend.plans.domain.entity;

import com.goldtrading.backend.common.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "plans")
public class Plan extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PlanType type;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(name="billing_cycle", nullable = false) private BillingCycle billingCycle;
    @Column(nullable = false) private BigDecimal price;
    @Column(name="profit_share_percent", nullable = false) private Integer profitSharePercent;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PlanStatus status;
}

