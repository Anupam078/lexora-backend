package com.lexora.lexora_backend.hearing;

import java.time.LocalDate;

public record UpdateHearingRequest(
        HearingStatus status,
        String adjournmentReason,
        LocalDate nextDate,
        String notes,
        String courtRoom
) {}