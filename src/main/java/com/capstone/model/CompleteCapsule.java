package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "complete_capsule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learner_capsule",
                        columnNames = {"learner_id", "capsule_id"}
                )
        },
        indexes = {
                @Index(name = "idx_complete_learner_id", columnList = "learner_id"),
                @Index(name = "idx_complete_capsule_id", columnList = "capsule_id"),
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
    @JoinColumn(name = "learner_id", nullable = false, updatable = false)
    @NotNull(message = "User is required")
    private UserSnapshot userSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id", nullable = false, updatable = false)
    @NotNull(message = "Capsule is required")
    private CapsuleSnapshot capsuleSnapshot;
}
