package com.capstone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "capsule_cluster_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_capsule_cluster",
                        columnNames = {"capsule_id", "cluster_id"}
                )
        },
        indexes = {
                @Index(name = "idx_capsule_mapping_capsule_idc", columnList = "capsule_id"),
                @Index(name = "idx_capsule_mapping_cluster_id", columnList = "cluster_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"skillCapsule", "theCluster"})
public class CapsuleClusterMapping {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Many-to-One relationship with CapsuleSnapshot
    @NotNull(message = "Skill capsule is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capsule_id",
            referencedColumnName = "capsule_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capsule_mapping_capsule")
    )
    @JsonIgnore
    private CapsuleSnapshot skillCapsule;

    // Many-to-One relationship with ClusterSnapshot
    @NotNull(message = "Skill cluster is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cluster_id",
            referencedColumnName = "cluster_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capsule_mapping_cluster")
    )
    private ClusterSnapshot theCluster;

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

