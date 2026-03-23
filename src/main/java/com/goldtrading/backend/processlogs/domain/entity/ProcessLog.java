package com.goldtrading.backend.processlogs.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "process_logs")
public class ProcessLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="mt5_account_id", nullable = false) private UUID mt5AccountId;
    @Column(name="port_id") private UUID portId;
    @Column(name="action_type", nullable = false) private String actionType;
    @Column(nullable = false) private String result;
    @Column(name="exit_code") private Integer exitCode;
    @Column(nullable = false) private String message;
    @Column(name="config_snapshot_json", columnDefinition = "text") private String configSnapshotJson;
    @CreationTimestamp @Column(name="created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
}

