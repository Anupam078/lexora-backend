package com.lexora.lexora_backend.document;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/cases/{caseId}")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable UUID caseId,
            @Valid @RequestBody UploadDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(caseId, request));
    }

    @GetMapping("/cases/{caseId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsForCase(
            @PathVariable UUID caseId) {
        return ResponseEntity.ok(documentService.getDocumentsForCase(caseId));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}