package com.lexora.lexora_backend.cases;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.createCase(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getCases() {
        return ResponseEntity.ok(caseService.getCases());
    }

    @PutMapping("/{caseId}")
    @PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> updateCase(@PathVariable UUID caseId,
                                           @RequestBody UpdateCaseRequest request) {
        return ResponseEntity.ok(caseService.updateCase(caseId, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CaseResponse> transitionStatus(
            @PathVariable UUID id,
            @RequestBody TransitionStatusRequest request) {

        CaseStatus newStatus = CaseStatus.valueOf(request.status().toUpperCase());
        CaseResponse response = caseService.transitionStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }
}