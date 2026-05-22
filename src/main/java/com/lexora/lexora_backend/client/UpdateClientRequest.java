package com.lexora.lexora_backend.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateClientRequest(

        String fullName,

        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Enter a valid 10-digit Indian mobile number"
        )
        String phone,

        @Email(message = "Enter a valid email address")
        String email,

        String address
) {}