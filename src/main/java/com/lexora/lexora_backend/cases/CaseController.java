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
    public ResponseEntity<Case> createCase(@RequestBody CreateCaseRequest request) {
        Case created = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getCases() {
        return ResponseEntity.ok(caseService.getCases());
    }

    @PutMapping("/{caseId}")
    @PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
    public ResponseEntity<Case> updateCase(@PathVariable UUID caseId,
                                           @RequestBody UpdateCaseRequest request) {
        return ResponseEntity.ok(caseService.updateCase(caseId, request));
    }
}