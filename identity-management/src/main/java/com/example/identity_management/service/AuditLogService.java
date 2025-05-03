package com.example.identity_management.service;

import com.example.identity_management.model.AuditLog;
import com.example.identity_management.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(String action, String username, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionContaining(action);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }
}