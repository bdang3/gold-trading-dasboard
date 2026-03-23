package com.goldtrading.backend.auditlogs.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="actor_type", nullable = false) private String actorType;
    @Column(name="actor_id") private String actorId;
    @Column(name="actor_name") private String actorName;
    @Column(nullable = false) private String action;
    @Column(name="entity_type", nullable = false) private String entityType;
    @Column(name="entity_id") private String entityId;
    @Column(nullable = false) private String result;
    @Column(nullable = false) private String message;
    @Column(name="metadata_json", columnDefinition = "text") private String metadataJson;
    @CreationTimestamp @Column(name="created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
}

