package com.lexora.lexora_backend.cases;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<Case, UUID> {
    List<Case> findAllByTenantId(UUID tenantId);
    List<Case> findAllByAdvocateIdAndTenantId(UUID advocateId, UUID tenantId);
    boolean existsByCaseNumberAndTenantId(String caseNumber, UUID tenantId);
}