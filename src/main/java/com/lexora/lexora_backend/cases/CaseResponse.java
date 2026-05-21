package com.lexora.lexora_backend.cases;

import com.lexora.lexora_backend.user.UserResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CaseResponse(
        UUID id,
        String caseNumber,
        String title,
        String status,
        String courtName,
        String petitionerName,
        String respondentName,
        LocalDate filingDate,
        UserResponse advocate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CaseResponse from(Case case_) {
        return new CaseResponse(
                case_.getId(),
                case_.getCaseNumber(),
                case_.getTitle(),
                case_.getStatus(),
                case_.getCourtName(),
                case_.getPetitionerName(),
                case_.getRespondentName(),
                case_.getFilingDate(),
                UserResponse.from(case_.getAdvocate()),
                case_.getCreatedAt(),
                case_.getUpdatedAt()
        );
    }
}