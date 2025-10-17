package com.capstone.repository;

import com.capstone.model.TrackCapsuleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrowthTrackCapsuleMappingRepository extends JpaRepository<TrackCapsuleMapping, UUID> {
    @Query("""
             SELECT tcm.growthTrack.growthTrackId, COUNT(tcm)
             FROM TrackCapsuleMapping tcm
             WHERE tcm.growthTrack.growthTrackId IN :trackIds
             GROUP BY tcm.growthTrack.growthTrackId
             """)
    List<Object[]> batchGetCapsuleCountsByTrackIds(@Param("trackIds") List<UUID> trackIds);
}
