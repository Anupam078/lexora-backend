package com.lexora.lexora_backend.cases;

import java.time.LocalDate;

public record CreateCaseRequest(
        String caseNumber,
        String title,
        String courtName,
        String petitionerName,
        String respondentName,
        LocalDate filingDate
) {}