package com.capstone.repository;

import com.capstone.model.CapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleSnapshotRepository extends JpaRepository<CapsuleSnapshot, UUID> {
    Optional<CapsuleSnapshot> findByCapsuleId(UUID capsuleId);
    boolean existsByCapsuleId(UUID capsuleId);
}
