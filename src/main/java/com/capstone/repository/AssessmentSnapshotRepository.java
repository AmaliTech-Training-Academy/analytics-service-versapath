package com.capstone.repository;

import com.capstone.model.AssessmentSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentSnapshotRepository extends JpaRepository<AssessmentSnapshot, UUID> {

    Optional<AssessmentSnapshot> findByUserSnapshotUserIdAndAssessmentIdAndAttemptNumber(
            UUID userId, UUID assessmentId, Integer attemptNumber);

    boolean existsByUserSnapshotUserIdAndAssessmentIdAndAttemptNumber(
            UUID userId, UUID assessmentId, Integer attemptNumber);

    Page<AssessmentSnapshot> findByUserSnapshotUserId(UUID userId, Pageable pageable);

    Page<AssessmentSnapshot> findByCapsuleSnapshotCapsuleId(UUID capsuleId, Pageable pageable);
}
