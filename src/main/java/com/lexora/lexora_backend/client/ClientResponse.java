package com.lexora.lexora_backend.client;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        String address,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFullName(),
                client.getPhone(),
                client.getEmail(),
                client.getAddress(),
                client.isActive(),
                client.getCreatedAt()
        );
    }
}