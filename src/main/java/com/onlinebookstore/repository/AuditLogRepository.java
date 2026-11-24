package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.AuditLogEntry;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    List<AuditLogEntry> findTop100ByOrderByCreatedAtDesc();
}
