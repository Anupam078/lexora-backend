package com.lexora.lexora_backend.hearing;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleHearingRequest(

        @NotNull(message = "Case ID is required")
        java.util.UUID caseId,

        @NotNull(message = "Scheduled date is required")
        @Future(message = "Hearing date must be in the future")
        LocalDate scheduledDate,

        LocalTime scheduledTime,
        String courtRoom,
        String purpose,
        String notes
) {}