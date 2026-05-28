package com.lexora.lexora_backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findAllByTenantIdOrderByPerformedAtDesc(UUID tenantId);
    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByPerformedAtDesc(
            String entityType, UUID entityId);
}