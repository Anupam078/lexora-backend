package com.lexora.lexora_backend.hearing;

import com.lexora.lexora_backend.cases.Case;
import com.lexora.lexora_backend.cases.CaseRepository;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class HearingService {

    private final HearingRepository hearingRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    public HearingService(HearingRepository hearingRepository,
                          CaseRepository caseRepository,
                          UserRepository userRepository) {
        this.hearingRepository = hearingRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    public HearingResponse scheduleHearing(ScheduleHearingRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        // Verify case exists in this tenant
        Case case_ = caseRepository.findById(request.caseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        if (!case_.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        // Verify advocate owns this case (admin can bypass)
        if (currentUser.getRole() == Role.ADVOCATE &&
                !case_.getAdvocate().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only schedule hearings for your own cases");
        }

        Hearing hearing = new Hearing();
        hearing.setHearingCase(case_);;
        hearing.setTenantId(tenantId);
        hearing.setScheduledDate(request.scheduledDate());
        hearing.setScheduledTime(request.scheduledTime());
        hearing.setCourtRoom(request.courtRoom());
        hearing.setPurpose(request.purpose());
        hearing.setNotes(request.notes());

        return HearingResponse.from(hearingRepository.save(hearing));
    }

    public List<HearingResponse> getHearingsForCase(UUID caseId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        Case case_ = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        if (!case_.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        // Advocate can only see hearings for their own cases
        if (currentUser.getRole() == Role.ADVOCATE &&
                !case_.getAdvocate().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return hearingRepository.findAllByHearingCaseId(caseId)
                .stream()
                .map(HearingResponse::from)
                .toList();
    }

    public List<HearingResponse> getUpcomingHearings() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return hearingRepository
                    .findAllByTenantIdAndScheduledDateGreaterThanEqual(
                            tenantId, LocalDate.now())
                    .stream()
                    .map(HearingResponse::from)
                    .toList();
        } else {
            return hearingRepository
                    .findAllByTenantIdAndHearingCaseAdvocateIdAndScheduledDateGreaterThanEqual(
                            tenantId, currentUser.getId(), LocalDate.now())
                    .stream()
                    .map(HearingResponse::from)
                    .toList();
        }
    }

    public HearingResponse updateHearing(UUID hearingId,
                                         UpdateHearingRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hearing not found"));

        if (!hearing.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        // Advocate can only update hearings on their own cases
        if (currentUser.getRole() == Role.ADVOCATE &&
                !hearing.getHearingCase().getAdvocate().getId()
                        .equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        if (request.status() != null) hearing.setStatus(request.status());
        if (request.adjournmentReason() != null)
            hearing.setAdjournmentReason(request.adjournmentReason());
        if (request.nextDate() != null) hearing.setNextDate(request.nextDate());
        if (request.notes() != null) hearing.setNotes(request.notes());
        if (request.courtRoom() != null) hearing.setCourtRoom(request.courtRoom());

        return HearingResponse.from(hearingRepository.save(hearing));
    }
}