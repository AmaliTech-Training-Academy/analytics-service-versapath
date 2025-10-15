package com.capstone.repository;

import com.capstone.model.AssessmentSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentSnapshotRepository extends JpaRepository<AssessmentSnapshot, UUID> {

    Optional<AssessmentSnapshot> findByUserSnapshotUserIdAndAssessmentIdAndAttemptNumber(
            UUID userId, UUID assessmentId, Integer attemptNumber);

    Page<AssessmentSnapshot> findByUserSnapshotUserId(UUID userId, Pageable pageable);

    Page<AssessmentSnapshot> findByCapsuleSnapshotCapsuleId(UUID capsuleId, Pageable pageable);

    @Query("""
              SELECT
                  AVG(a.score),
                  COUNT(a),
                  COUNT(CASE WHEN a.score >= 70 THEN 1 ELSE NULL END)
              FROM AssessmentSnapshot a
              WHERE a.timeFinish >= :startDate AND a.timeFinish <= :endDate
              """)
    List<Object[]> getDashboardSummaryStats(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);


    // ENGAGED LEARNERS METRICS

    @SuppressWarnings("SqlNoDataSourceInspection")
    @Query(value = """
                  SELECT
                      EXTRACT(MONTH FROM time_finish) as month,
                      COUNT(DISTINCT user_id) as engaged_learners
                  FROM assessment_snapshot
                  WHERE time_finish >= :startDate AND time_finish <= :endDate
                  GROUP BY EXTRACT(MONTH FROM time_finish)
                  ORDER BY month
                  """, nativeQuery = true)
    List<Object[]> getMonthlyEngagedLearners(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query(value = """
                  SELECT
                      EXTRACT(QUARTER FROM time_finish) as quarter,
                      COUNT(DISTINCT user_id) as engaged_learners
                  FROM assessment_snapshot
                  WHERE time_finish >= :startDate AND time_finish <= :endDate
                  GROUP BY EXTRACT(QUARTER FROM time_finish)
                  ORDER BY quarter
                  """, nativeQuery = true)
    List<Object[]> getQuarterlyEngagedLearners(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query(value = """
                  SELECT
                      EXTRACT(YEAR FROM time_finish) as year,
                      COUNT(DISTINCT user_id) as engaged_learners
                  FROM assessment_snapshot
                  WHERE time_finish >= :startDate AND time_finish <= :endDate
                  GROUP BY EXTRACT(YEAR FROM time_finish)
                  ORDER BY year
                  """, nativeQuery = true)
    List<Object[]> getYearlyEngagedLearners(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // SCORE DISTRIBUTION

    @Query(value = """
              SELECT
                  CASE
                      WHEN score >= 90 THEN '90-100'
                      WHEN score >= 80 THEN '80-89'
                      WHEN score >= 70 THEN '70-79'
                      WHEN score >= 60 THEN '60-69'
                      ELSE '<60'
                  END as score_range,
                  COUNT(DISTINCT user_id) as learner_count
              FROM assessment_snapshot
              WHERE time_finish >= :startDate AND time_finish <= :endDate
              GROUP BY
                  CASE
                      WHEN score >= 90 THEN '90-100'
                      WHEN score >= 80 THEN '80-89'
                      WHEN score >= 70 THEN '70-79'
                      WHEN score >= 60 THEN '60-69'
                      ELSE '<60'
                  END
              ORDER BY
                  MIN(CASE
                      WHEN score >= 90 THEN 1
                      WHEN score >= 80 THEN 2
                      WHEN score >= 70 THEN 3
                      WHEN score >= 60 THEN 4
                      ELSE 5
                  END)
              """, nativeQuery = true)
    List<Object[]> getScoreDistributionData(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
                  SELECT COUNT(DISTINCT user_id)
                  FROM assessment_snapshot
                  WHERE time_finish >= :startDate AND time_finish <= :endDate
                  AND user_id IN (
                      SELECT user_id
                      FROM assessment_snapshot
                      WHERE time_finish >= :startDate AND time_finish <= :endDate
                      GROUP BY user_id
                      HAVING AVG(score) >= :threshold
                  )
                  """, nativeQuery = true)
    Long countUsersWithHighPerformance(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("threshold") Double threshold);

}
