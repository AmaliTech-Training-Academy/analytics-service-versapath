package com.capstone.repository;

import com.capstone.model.CapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CapsuleSnapshotRepository extends JpaRepository<CapsuleSnapshot, UUID> {
}
