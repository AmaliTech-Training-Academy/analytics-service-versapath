package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "complete_capsule",
        indexes = {
                @Index(name = "idx_complete_learner_id", columnList = "learner_id"),
                @Index(name = "idx_complete_capsule_id", columnList = "capsule_id"),

                @Index(name = "idx_complete_completed_at", columnList = "completed_at"),
                @Index(name = "idx_complete_monthly_trends", columnList = "completed_at, capsule_id"),
                @Index(name = "idx_complete_user_timeline", columnList = "learner_id, completed_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteCapsule {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "learner_id",
            referencedColumnName = "user_id",
            nullable = false,
            updatable = false
    )
    @NotNull(message = "User is required")
    private UserSnapshot userSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "capsule_id",
            referencedColumnName = "capsule_id",
            nullable = false,
            updatable = false
    )
    @NotNull(message = "Capsule is required")
    private CapsuleSnapshot capsuleSnapshot;

    @NotNull(message = "Completion time is required")
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}
