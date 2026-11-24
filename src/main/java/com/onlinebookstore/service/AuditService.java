package com.onlinebookstore.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.AuditLogEntry;
import com.onlinebookstore.repository.AuditLogRepository;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** Records an action in its own transaction so it survives rollbacks elsewhere. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String actionType, String detail) {
        try {
            auditLogRepository.save(new AuditLogEntry(actor, actionType, detail));
        } catch (RuntimeException ex) {
            log.warn("Could not write audit log entry for {} {}", actionType, detail, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntry> recent() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
