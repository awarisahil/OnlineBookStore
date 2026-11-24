package com.onlinebookstore.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor", nullable = false, length = 200)
    private String actor;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLogEntry() {
    }

    public AuditLogEntry(String actor, String actionType, String detail) {
        this.actor = actor;
        this.actionType = actionType;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
