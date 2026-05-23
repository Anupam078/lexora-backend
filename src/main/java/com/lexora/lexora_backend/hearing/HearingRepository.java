package com.lexora.lexora_backend.hearing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HearingRepository extends JpaRepository<Hearing, UUID> {
    List<Hearing> findAllByCaseId(UUID caseId);
    List<Hearing> findAllByTenantIdAndScheduledDateGreaterThanEqual(
            UUID tenantId, LocalDate date);
    List<Hearing> findAllByTenantIdAndCase_AdvocateIdAndScheduledDateGreaterThanEqual(
            UUID tenantId, UUID advocateId, LocalDate date);
}