package com.capstone.repository;

import com.capstone.model.CapsuleClusterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CapsuleClusterMappingRepository extends JpaRepository<CapsuleClusterMapping, UUID> {
}
