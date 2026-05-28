package com.lexora.lexora_backend.audit;

import com.lexora.lexora_backend.tenant.TenantContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String entityType,
                    UUID entityId,
                    AuditAction action,
                    String oldValue,
                    String newValue) {

        String tenantIdStr = TenantContext.getTenantId();
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        AuditLog log = new AuditLog();
        log.setTenantId(UUID.fromString(tenantIdStr));
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(UUID.fromString(userId));

        auditLogRepository.save(log);
    }
}