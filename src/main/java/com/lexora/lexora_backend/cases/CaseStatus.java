package com.lexora.lexora_backend.cases;

import java.util.Set;

public enum CaseStatus {

    FILED(Set.of("ACTIVE")),
    ACTIVE(Set.of("HEARING_SCHEDULED")),
    HEARING_SCHEDULED(Set.of("ADJOURNED", "DISPOSED")),
    ADJOURNED(Set.of("HEARING_SCHEDULED", "DISPOSED")),
    DISPOSED(Set.of());

    private final Set<String> allowedTransitions;

    CaseStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(CaseStatus next) {
        return allowedTransitions.contains(next.name());
    }
}