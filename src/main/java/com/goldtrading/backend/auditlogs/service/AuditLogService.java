package com.goldtrading.backend.auditlogs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldtrading.backend.auditlogs.domain.entity.AuditLog;
import com.goldtrading.backend.auditlogs.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(String actorType, String actorId, String actorName, String action, String entityType, String entityId,
                    String result, String message, Map<String, Object> metadata) {
        AuditLog log = new AuditLog();
        log.setActorType(actorType);
        log.setActorId(actorId);
        log.setActorName(actorName);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setResult(result);
        log.setMessage(message);
        try {
            log.setMetadataJson(metadata == null ? null : objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            log.setMetadataJson("{}");
        }
        auditLogRepository.save(log);
    }
}

