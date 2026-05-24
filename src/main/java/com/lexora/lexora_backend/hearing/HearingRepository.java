package com.lexora.lexora_backend.hearing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HearingRepository extends JpaRepository<Hearing, UUID> {
    List<Hearing> findAllByHearingCaseId(UUID caseId);
    List<Hearing> findAllByTenantIdAndScheduledDateGreaterThanEqual(
            UUID tenantId, LocalDate date);
    List<Hearing> findAllByTenantIdAndHearingCaseAdvocateIdAndScheduledDateGreaterThanEqual(
            UUID tenantId, UUID advocateId, LocalDate date);
}