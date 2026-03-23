package com.goldtrading.backend.notifications.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String message;
    @Column(name="read_at") private OffsetDateTime readAt;
    @CreationTimestamp @Column(name="created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
}

