package com.lexora.lexora_backend.document;

import com.lexora.lexora_backend.cases.Case;
import com.lexora.lexora_backend.cases.CaseRepository;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository,
                           CaseRepository caseRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Case getVerifiedCase(UUID caseId, UUID tenantId, User currentUser) {
        Case case_ = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        if (!case_.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        if (currentUser.getRole() == Role.ADVOCATE &&
                !case_.getAdvocate().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return case_;
    }

    public DocumentResponse uploadDocument(UUID caseId,
                                           UploadDocumentRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();
        Case case_ = getVerifiedCase(caseId, tenantId, currentUser);

        Document document = new Document();
        document.setDocumentCase(case_);
        document.setTenantId(tenantId);
        document.setFileName(request.fileName());
        document.setFileKey(request.fileKey());
        document.setFileType(request.fileType());
        document.setUploadedBy(currentUser);

        return DocumentResponse.from(documentRepository.save(document));
    }

    public List<DocumentResponse> getDocumentsForCase(UUID caseId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();
        getVerifiedCase(caseId, tenantId, currentUser);

        return documentRepository
                .findAllByDocumentCaseIdAndTenantId(caseId, tenantId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse getDocument(UUID documentId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        Document document = documentRepository
                .findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found"));

        // Advocate can only view documents on their own cases
        if (currentUser.getRole() == Role.ADVOCATE &&
                !document.getDocumentCase().getAdvocate().getId()
                        .equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return DocumentResponse.from(document);
    }

    public void deleteDocument(UUID documentId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        Document document = documentRepository
                .findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found"));

        // Only the uploader or ADMIN can delete
        if (currentUser.getRole() == Role.ADVOCATE &&
                !document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only delete documents you uploaded");
        }

        documentRepository.delete(document);
    }
}