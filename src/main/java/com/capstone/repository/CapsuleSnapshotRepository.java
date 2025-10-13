package com.capstone.repository;

import com.capstone.model.CapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleSnapshotRepository extends JpaRepository<CapsuleSnapshot, UUID> {
    Optional<CapsuleSnapshot> findByCapsuleId(UUID capsuleId);
    boolean existsByCapsuleId(UUID capsuleId);
    @Query("""
             SELECT DISTINCT cs FROM CapsuleSnapshot cs
             LEFT JOIN FETCH cs.capsuleAtomMappings cam
             LEFT JOIN FETCH cam.skillAtom sa
             WHERE cs.capsuleId = :capsuleId
             """)
    Optional<CapsuleSnapshot> findByCapsuleIdWithAtomMappings(@Param("capsuleId") UUID capsuleId);

}
