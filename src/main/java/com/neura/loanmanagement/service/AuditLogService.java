package com.neura.loanmanagement.service;

import com.neura.loanmanagement.entity.AuditLog;

import java.util.List;

public interface AuditLogService {

    void logAction(String entityName, String entityId, String action, String performedBy, String details, String clientIp);

    List<AuditLog> getLogsForEntity(String entityName, String entityId);
}
