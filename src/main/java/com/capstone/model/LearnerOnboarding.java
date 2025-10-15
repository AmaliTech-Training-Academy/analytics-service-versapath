package com.capstone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "learner_onboarding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerOnboarding {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "learner_id",
            referencedColumnName = "user_id",
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_learner_onboarding_user")
    )
    private UserSnapshot learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "talent_route_id",
            referencedColumnName = "talent_route_id",
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_learner_onboarding_talent_route")
    )
    private TalentRouteSnapshot talentRoute;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
