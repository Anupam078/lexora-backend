package com.lexora.lexora_backend.document;

import com.lexora.lexora_backend.user.UserResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID caseId,
        String caseNumber,
        String fileName,
        String fileKey,
        String fileType,
        UserResponse uploadedBy,
        LocalDateTime uploadedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentCase().getId(),
                document.getDocumentCase().getCaseNumber(),
                document.getFileName(),
                document.getFileKey(),
                document.getFileType(),
                UserResponse.from(document.getUploadedBy()),
                document.getUploadedAt()
        );
    }
}