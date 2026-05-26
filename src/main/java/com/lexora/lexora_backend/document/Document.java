package com.lexora.lexora_backend.document;

import com.lexora.lexora_backend.cases.Case;
import com.lexora.lexora_backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case documentCase;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_key", nullable = false)
    private String fileKey;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}