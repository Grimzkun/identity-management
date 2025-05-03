package com.example.identity_management.repository;

import com.example.identity_management.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsername(String username);

    List<AuditLog> findByActionContaining(String action);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}