package com.lexora.lexora_backend.cases;

public record UpdateCaseRequest(
        String title,
        String status,
        String courtName
) {}