package com.capstone.repository;

import com.capstone.model.CompleteCapsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompleteCapsuleRepository extends JpaRepository<CompleteCapsule, UUID> {
    boolean existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);
    Optional<CompleteCapsule> findByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);
}
