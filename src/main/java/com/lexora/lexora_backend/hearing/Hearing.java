package com.lexora.lexora_backend.hearing;

import com.lexora.lexora_backend.cases.Case;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "hearings")
@Data
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case hearingCase;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "court_room")
    private String courtRoom;

    @Column
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HearingStatus status = HearingStatus.SCHEDULED;

    @Column(name = "adjournment_reason")
    private String adjournmentReason;

    @Column(name = "next_date")
    private LocalDate nextDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}