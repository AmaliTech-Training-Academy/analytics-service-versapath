package com.capstone.repository;

import com.capstone.model.CapsuleAtomMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CapsuleAtomMappingRepository extends JpaRepository<CapsuleAtomMapping, UUID> {
}
