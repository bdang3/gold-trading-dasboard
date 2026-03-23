package com.goldtrading.backend.plans.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_plan_history")
public class UserPlanHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="user_id", nullable = false) private UUID userId;
    @Column(name="plan_id", nullable = false) private UUID planId;
    @Column(name="started_at", nullable = false) private OffsetDateTime startedAt;
    @Column(name="ended_at") private OffsetDateTime endedAt;
    @Column(name="is_current", nullable = false) private Boolean isCurrent;
    @CreationTimestamp @Column(name="created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
}

