package com.lexora.lexora_backend.auth;

public record CreateAdvocateRequest(
        String fullName,
        String email,
        String password
) {}