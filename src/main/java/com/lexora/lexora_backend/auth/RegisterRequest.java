package com.lexora.lexora_backend.auth;

public record RegisterRequest(
        String tenantName,
        String subdomain,
        String fullName,
        String email,
        String password
) {}