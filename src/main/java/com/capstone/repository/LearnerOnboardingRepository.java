package com.capstone.repository;

import com.capstone.model.LearnerOnboarding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearnerOnboardingRepository extends JpaRepository<LearnerOnboarding, UUID> {

    @Query("SELECT COUNT(lo) > 0 FROM LearnerOnboarding lo WHERE lo.learner.userId = :learnerId")
    boolean existsByLearnerId(@Param("learnerId") UUID learnerId);

    // Simple: Get all learners
    @Query("""
                  SELECT lo
                  FROM LearnerOnboarding lo
                  JOIN FETCH lo.learner l
                  JOIN FETCH lo.talentRoute tr
                  JOIN FETCH lo.growthTrack gt
                  """)
    Page<LearnerOnboarding> findAllWithDetails(Pageable pageable);

    // Simple: Search by keyword
    @Query("""
                  SELECT lo
                  FROM LearnerOnboarding lo
                  JOIN FETCH lo.learner l
                  JOIN FETCH lo.talentRoute tr
                  JOIN FETCH lo.growthTrack gt
                  WHERE LOWER(l.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(tr.routeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  """)
    Page<LearnerOnboarding> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Simple: Filter by talent route
    @Query("""
                  SELECT lo
                  FROM LearnerOnboarding lo
                  JOIN FETCH lo.learner l
                  JOIN FETCH lo.talentRoute tr
                  JOIN FETCH lo.growthTrack gt
                  WHERE tr.talentRouteId = :talentRouteId
                  """)
    Page<LearnerOnboarding> findByTalentRoute(@Param("talentRouteId") UUID talentRouteId, Pageable pageable);

    // Simple: Filter by learner IDs
    @Query("""
                  SELECT lo
                  FROM LearnerOnboarding lo
                  JOIN FETCH lo.learner l
                  JOIN FETCH lo.talentRoute tr
                  JOIN FETCH lo.growthTrack gt
                  WHERE l.userId IN :learnerIds
                  """)
    Page<LearnerOnboarding> findByLearnerIds(@Param("learnerIds") List<UUID> learnerIds, Pageable pageable);

    @Query("""
                  SELECT
                      tr.talentRouteId,
                      tr.routeName,
                      COUNT(DISTINCT lo.learner.userId)
                  FROM LearnerOnboarding lo
                  JOIN lo.talentRoute tr
                  GROUP BY tr.talentRouteId, tr.routeName
                  """)
    List<Object[]> getHeatmapAggregates();

    @Query("""
                  SELECT
                      AVG(a.score),
                      SUM(CASE WHEN a.score < 70 THEN 1 ELSE 0 END),
                      SUM(CASE WHEN a.score >= 70 AND a.score < 85 THEN 1 ELSE 0 END),
                      SUM(CASE WHEN a.score >= 85 THEN 1 ELSE 0 END)
                  FROM LearnerOnboarding lo
                  JOIN AssessmentSnapshot a ON a.userSnapshot.userId = lo.learner.userId
                  JOIN TrackCapsuleMapping tcm ON tcm.skillCapsule.capsuleId = a.capsuleSnapshot.capsuleId
                  WHERE lo.talentRoute.talentRouteId = :talentRouteId
                  AND tcm.growthTrack.growthTrackId = lo.growthTrack.growthTrackId
                  """)
    List<Object[]> getRouteReadinessStats(@Param("talentRouteId") UUID talentRouteId);

    @Query("""
                  SELECT DISTINCT tr.talentRouteId, tr.routeName
                  FROM LearnerOnboarding lo
                  JOIN lo.talentRoute tr
                  ORDER BY tr.routeName
                  """)
    List<Object[]> getAvailableTalentRoutes();

}
