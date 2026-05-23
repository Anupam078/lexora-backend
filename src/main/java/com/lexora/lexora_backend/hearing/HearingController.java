package com.lexora.lexora_backend.hearing;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hearings")
@PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
public class HearingController {

    private final HearingService hearingService;

    public HearingController(HearingService hearingService) {
        this.hearingService = hearingService;
    }

    @PostMapping
    public ResponseEntity<HearingResponse> scheduleHearing(
            @Valid @RequestBody ScheduleHearingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hearingService.scheduleHearing(request));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<HearingResponse>> getHearingsForCase(
            @PathVariable UUID caseId) {
        return ResponseEntity.ok(hearingService.getHearingsForCase(caseId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<HearingResponse>> getUpcomingHearings() {
        return ResponseEntity.ok(hearingService.getUpcomingHearings());
    }

    @PutMapping("/{hearingId}")
    public ResponseEntity<HearingResponse> updateHearing(
            @PathVariable UUID hearingId,
            @RequestBody UpdateHearingRequest request) {
        return ResponseEntity.ok(
                hearingService.updateHearing(hearingId, request));
    }
}