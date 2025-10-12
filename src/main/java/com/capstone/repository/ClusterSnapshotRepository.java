package com.capstone.repository;

import com.capstone.model.ClusterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClusterSnapshotRepository extends JpaRepository<ClusterSnapshot, UUID> {
    Optional<ClusterSnapshot> findByClusterId(UUID clusterId);
}
