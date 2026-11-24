package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.entity.AuditLog;
import com.neura.loanmanagement.repository.AuditLogRepository;
import com.neura.loanmanagement.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityName, String entityId, String action, String performedBy, String details, String clientIp) {
        try {
            AuditLog auditLog = new AuditLog(entityName, entityId, action, performedBy, details, clientIp);
            auditLogRepository.save(auditLog);
            log.info("AUDIT: [{}] {} on {} id={} by {}", action, details, entityName, entityId, performedBy);
        } catch (Exception ex) {
            log.error("Failed to persist audit log: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForEntity(String entityName, String entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }
}
