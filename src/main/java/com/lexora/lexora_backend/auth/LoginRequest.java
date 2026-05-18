package com.lexora.lexora_backend.auth;

public record LoginRequest(
        String subdomain,
        String email,
        String password
) {}