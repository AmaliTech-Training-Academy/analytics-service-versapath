package com.capstone.repository;

import com.capstone.model.CapsuleClusterMapping;
import com.capstone.model.ClusterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapsuleClusterMappingRepository extends JpaRepository<CapsuleClusterMapping, UUID> {

    @Query("""
             SELECT tcm.growthTrack.growthTrackId, ccm.theCluster
             FROM TrackCapsuleMapping tcm
             JOIN CapsuleClusterMapping ccm ON ccm.skillCapsule.capsuleId = tcm.skillCapsule.capsuleId
             WHERE tcm.growthTrack.growthTrackId IN :trackIds
             ORDER BY tcm.growthTrack.growthTrackId, ccm.theCluster.name
             """)
    List<Object[]> batchGetClustersByTrackIds(@Param("trackIds") List<UUID> trackIds);

    @Query("SELECT DISTINCT ccm.theCluster FROM CapsuleClusterMapping ccm ORDER BY ccm.theCluster.name")
    List<ClusterSnapshot> findAllUniqueClusters();

    @Query("""
                 SELECT DISTINCT lo.learner.userId
                 FROM LearnerOnboarding lo
                 JOIN TrackCapsuleMapping tcm ON tcm.growthTrack.growthTrackId = lo.growthTrack.growthTrackId
                 JOIN CapsuleClusterMapping ccm ON ccm.skillCapsule.capsuleId = tcm.skillCapsule.capsuleId
                 WHERE ccm.theCluster.clusterId IN :clusterIds
                 """)
    List<UUID> findLearnerIdsByClusterIds(@Param("clusterIds") List<UUID> clusterIds);
}
