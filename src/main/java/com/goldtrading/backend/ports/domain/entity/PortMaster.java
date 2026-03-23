package com.goldtrading.backend.ports.domain.entity;

import com.goldtrading.backend.common.AuditableEntity;
import com.goldtrading.backend.common.PortStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "port_master")
public class PortMaster extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String code;
    @Column(name="ip_address", nullable = false) private String ipAddress;
    @Column(name="port_number", nullable = false) private Integer portNumber;
    @Column(nullable = false) private String environment;
    @Column(name="broker_binding") private String brokerBinding;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PortStatus status;
    @Column(name="current_mt5_account_id") private UUID currentMt5AccountId;
    private String note;
}

