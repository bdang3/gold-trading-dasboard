package com.goldtrading.backend.mt5accounts.domain.entity;

import com.goldtrading.backend.common.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mt5_accounts")
public class MT5Account extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="user_id", nullable = false) private UUID userId;
    @Column(name="account_number", nullable = false) private String accountNumber;
    @Column(name="encrypted_password", nullable = false) private String encryptedPassword;
    @Column(nullable = false) private String broker;
    @Column(nullable = false) private String server;
    @Enumerated(EnumType.STRING) @Column(name="account_type", nullable = false) private AccountType accountType;
    @Enumerated(EnumType.STRING) @Column(name="verification_status", nullable = false) private VerificationStatus verificationStatus;
    @Column(name="verification_message") private String verificationMessage;
    @Column(name="strategy_id") private UUID strategyId;
    private String timeframe;
    @Column(name="risk_rule_id") private UUID riskRuleId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AccountStatus status;
    @Enumerated(EnumType.STRING) @Column(name="admin_action", nullable = false) private AdminActionState adminAction;
    @Column(name="assigned_port_id") private UUID assignedPortId;
    @Column(name="submitted_at") private OffsetDateTime submittedAt;
    @Column(name="started_at") private OffsetDateTime startedAt;
    @Column(name="stopped_at") private OffsetDateTime stoppedAt;
    @Column(name="last_config_updated_at") private OffsetDateTime lastConfigUpdatedAt;
    @Column(name="deleted_at") private OffsetDateTime deletedAt;
}

