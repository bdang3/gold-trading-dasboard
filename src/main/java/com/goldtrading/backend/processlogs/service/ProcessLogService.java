package com.goldtrading.backend.processlogs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldtrading.backend.processlogs.domain.entity.ProcessLog;
import com.goldtrading.backend.processlogs.repository.ProcessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessLogService {
    private final ProcessLogRepository processLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(UUID accountId, UUID portId, String actionType, String result, Integer exitCode, String message, Map<String, Object> snapshot) {
        ProcessLog log = new ProcessLog();
        log.setId(UUID.randomUUID());
        log.setMt5AccountId(accountId);
        log.setPortId(portId);
        log.setActionType(actionType);
        log.setResult(result);
        log.setExitCode(exitCode);
        log.setMessage(message);
        try {
            log.setConfigSnapshotJson(snapshot == null ? null : objectMapper.writeValueAsString(snapshot));
        } catch (Exception e) {
            log.setConfigSnapshotJson("{}");
        }
        processLogRepository.save(log);
    }
}

