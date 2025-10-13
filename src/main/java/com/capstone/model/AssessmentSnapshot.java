package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "assessment_snapshot",
        indexes = {
                @Index(name = "idx_assessment_user", columnList = "user_id"),
                @Index(name = "idx_assessment_capsule", columnList = "capsule_id"),
                @Index(name = "idx_assessment_assessment_id", columnList = "assessment_id"),
                @Index(name = "idx_assessment_time_finish", columnList = "time_finish"),
                @Index(name = "idx_assessment_user_time", columnList = "user_id, time_finish"),
                @Index(name = "idx_assessment_capsule_time", columnList = "capsule_id, time_finish"),
                @Index(name = "idx_assessment_moodle_quiz", columnList = "moodle_quiz_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_assessment_attempt",
                        columnNames = {"user_id", "assessment_id", "attempt_number"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"userSnapshot", "capsuleSnapshot"})
public class AssessmentSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Assessment ID is required")
    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;

    @NotBlank(message = "Assessment name is required")
    @Size(max = 255, message = "Assessment name must not exceed 255 characters")
    @Column(name = "assessment_name", nullable = false)
    private String assessmentName;

    @NotNull(message = "Moodle Quiz ID is required")
    @Column(name = "moodle_quiz_id", nullable = false)
    private Integer moodleQuizId;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 100, message = "Score cannot exceed 100")
    @Column(name = "score", nullable = false)
    private Double score;

    @NotNull(message = "Time start is required")
    @Column(name = "time_start", nullable = false)
    private LocalDateTime timeStart;

    @NotNull(message = "Time finish is required")
    @Column(name = "time_finish", nullable = false)
    private LocalDateTime timeFinish;

    @NotNull(message = "Attempt number is required")
    @Min(value = 1, message = "Attempt number must be at least 1")
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_assessment_user")
    )
    private UserSnapshot userSnapshot;

    @NotNull(message = "Capsule is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capsule_id",
            referencedColumnName = "capsule_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_assessment_capsule")
    )
    private CapsuleSnapshot capsuleSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
