package com.goldtrading.backend.auditlogs.repository;

import com.goldtrading.backend.auditlogs.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByActionContainingIgnoreCaseOrActorNameContainingIgnoreCase(String action, String actorName, Pageable pageable);
}
