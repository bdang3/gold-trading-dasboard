package com.goldtrading.backend.trades.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Immutable
@Table(schema = "public", name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_id", nullable = false)
    private Long positionId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 8)
    private String direction;

    @Column(name = "lots", precision = 18, scale = 4)
    private BigDecimal lots;

    @Column(name = "entry_price", precision = 18, scale = 5)
    private BigDecimal entryPrice;

    @Column(name = "exit_price", precision = 18, scale = 5)
    private BigDecimal exitPrice;

    @Column(name = "pnl", precision = 18, scale = 2)
    private BigDecimal pnl;

    @Column(name = "exit_reason", length = 64)
    private String exitReason;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "account", length = 64)
    private String account;
}

