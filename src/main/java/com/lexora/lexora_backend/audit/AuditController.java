package com.lexora.lexora_backend.audit;

import com.lexora.lexora_backend.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        return ResponseEntity.ok(
                auditLogRepository.findAllByTenantIdOrderByPerformedAtDesc(tenantId));
    }

    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVOCATE')")
    public ResponseEntity<List<AuditLog>> getLogsForEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        return ResponseEntity.ok(
                auditLogRepository
                        .findAllByEntityTypeAndEntityIdOrderByPerformedAtDesc(
                                entityType, entityId));
    }
}