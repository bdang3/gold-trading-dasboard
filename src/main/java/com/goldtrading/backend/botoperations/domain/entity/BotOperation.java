package com.goldtrading.backend.botoperations.domain.entity;

import com.goldtrading.backend.common.BotOperationType;
import com.goldtrading.backend.common.OperationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bot_operations")
public class BotOperation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="mt5_account_id", nullable = false) private UUID mt5AccountId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BotOperationType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OperationStatus status;
    @Column(name="requested_by_type", nullable = false) private String requestedByType;
    @Column(name="requested_by_id", nullable = false) private String requestedById;
    @Column(name="port_id") private UUID portId;
    @Column(name="payload_json", columnDefinition = "text") private String payloadJson;
    @Column(name="result_json", columnDefinition = "text") private String resultJson;
    @CreationTimestamp @Column(name="created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Column(name="completed_at") private OffsetDateTime completedAt;
}

