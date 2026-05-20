package com.lexora.lexora_backend.cases;

import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cases")
@Data
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id")
    private User advocate;

    @Column(name = "case_number", nullable = false)
    private String caseNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status;

    @Column(name = "court_name", nullable = false)
    private String courtName;

    @Column(name = "petitioner_name", nullable = false)
    private String petitionerName;

    @Column(name = "respondent_name", nullable = false)
    private String respondentName;

    @Column(name = "filing_date", nullable = false)
    private LocalDate filingDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}