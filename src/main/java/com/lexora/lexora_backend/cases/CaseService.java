package com.lexora.lexora_backend.cases;

import com.lexora.lexora_backend.audit.AuditAction;
import com.lexora.lexora_backend.audit.AuditService;
import com.lexora.lexora_backend.exception.InvalidTransitionException;
import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.tenant.TenantRepository;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public CaseService(CaseRepository caseRepository,
                       TenantRepository tenantRepository,
                       UserRepository userRepository,
                       AuditService auditService) {
        this.caseRepository = caseRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public CaseResponse  createCase(CreateCaseRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        // Get current user from SecurityContext
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (caseRepository.existsByCaseNumberAndTenantId(
                request.caseNumber(), tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Case number already exists");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant not found"));

        User advocate = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Case newCase = new Case();
        newCase.setTenant(tenant);
        newCase.setAdvocate(advocate);
        newCase.setCaseNumber(request.caseNumber());
        newCase.setTitle(request.title());
        newCase.setStatus(CaseStatus.ACTIVE);
        newCase.setCourtName(request.courtName());
        newCase.setPetitionerName(request.petitionerName());
        newCase.setRespondentName(request.respondentName());
        newCase.setFilingDate(request.filingDate());

        Case saved = caseRepository.save(newCase);
        auditService.log("CASE", saved.getId(), AuditAction.CREATED,
                null, saved.getCaseNumber());
        return CaseResponse.from(saved);


    }

    public List<CaseResponse> getCases() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // Get current user to check role
        User currentUser = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // ADMIN sees all cases, ADVOCATE sees only their own
        if (currentUser.getRole() == Role.ADMIN) {
            return caseRepository.findAllByTenantId(tenantId)
                    .stream()
                    .map(CaseResponse::from)
                    .toList();
        } else {
            return caseRepository.findAllByAdvocateIdAndTenantId(
                            UUID.fromString(userId), tenantId)
                    .stream()
                    .map(CaseResponse::from)
                    .toList();
        }
    }

    public CaseResponse  updateCase(UUID caseId, UpdateCaseRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Case existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        // Make sure case belongs to this tenant
        if (!existingCase.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        existingCase.setTitle(request.title());
        existingCase.setStatus(CaseStatus.valueOf(request.status().toUpperCase()));
        existingCase.setCourtName(request.courtName());

        Case saved = caseRepository.save(existingCase);
        auditService.log("CASE", saved.getId(), AuditAction.UPDATED,
                null, saved.getTitle());
        return CaseResponse.from(saved);


    }

    @org.springframework.transaction.annotation.Transactional
    public CaseResponse transitionStatus(UUID caseId, CaseStatus newStatus) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Case existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        if (!existingCase.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        CaseStatus currentStatus = existingCase.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidTransitionException(
                    currentStatus.name(),
                    newStatus.name()
            );
        }

        existingCase.setStatus(newStatus);
        Case saved = caseRepository.save(existingCase);
        auditService.log("CASE", saved.getId(), AuditAction.STATUS_CHANGED,
                currentStatus.name(), newStatus.name());
        return CaseResponse.from(saved);

    }
}