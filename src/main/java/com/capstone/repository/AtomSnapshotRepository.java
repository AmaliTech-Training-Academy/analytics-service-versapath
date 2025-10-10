package com.capstone.repository;

import com.capstone.model.AtomSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AtomSnapshotRepository extends JpaRepository<AtomSnapshot, UUID> {
}
