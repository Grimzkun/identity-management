package com.example.identity_management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String username;

    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}