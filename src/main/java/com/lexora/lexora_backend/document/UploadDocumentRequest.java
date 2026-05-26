package com.lexora.lexora_backend.document;

import jakarta.validation.constraints.NotBlank;

public record UploadDocumentRequest(
        @NotBlank(message = "File name is required")
        String fileName,

        @NotBlank(message = "File key is required")
        String fileKey,

        String fileType
) {}