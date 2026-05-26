package com.lexora.lexora_backend.document;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findAllByDocumentCaseIdAndTenantId(UUID caseId, UUID tenantId);
    Optional<Document> findByIdAndTenantId(UUID id, UUID tenantId);
}