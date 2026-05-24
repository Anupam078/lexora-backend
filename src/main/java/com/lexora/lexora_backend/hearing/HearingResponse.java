package com.lexora.lexora_backend.hearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record HearingResponse(
        UUID id,
        UUID caseId,
        String caseNumber,
        String caseTitle,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        String courtRoom,
        String purpose,
        HearingStatus status,
        String adjournmentReason,
        LocalDate nextDate,
        String notes,
        LocalDateTime createdAt
) {
    public static HearingResponse from(Hearing hearing) {
        return new HearingResponse(
                hearing.getId(),
                hearing.getHearingCase().getId(),
                hearing.getHearingCase().getCaseNumber(),
                hearing.getHearingCase().getTitle(),
                hearing.getScheduledDate(),
                hearing.getScheduledTime(),
                hearing.getCourtRoom(),
                hearing.getPurpose(),
                hearing.getStatus(),
                hearing.getAdjournmentReason(),
                hearing.getNextDate(),
                hearing.getNotes(),
                hearing.getCreatedAt()
        );
    }
}