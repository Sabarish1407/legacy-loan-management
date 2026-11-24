package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
}
