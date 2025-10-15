package com.capstone.repository;

import com.capstone.model.CompleteCapsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompleteCapsuleRepository extends JpaRepository<CompleteCapsule, UUID> {

    boolean existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);


    @Query(value = """
             SELECT COUNT(*)
             FROM complete_capsule
             WHERE completed_at >= :startDate AND completed_at <= :endDate
             """, nativeQuery = true)
    Long countCompletedCapsulesInPeriod(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query(value = """
             SELECT
                 EXTRACT(MONTH FROM completed_at) as month,
                 COUNT(*) as completion_count
             FROM complete_capsule
             WHERE completed_at >= :startDate AND completed_at <= :endDate
             GROUP BY EXTRACT(MONTH FROM completed_at)
             ORDER BY month
             """, nativeQuery = true)
    List<Object[]> getMonthlyCompletionCounts(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    @Query(value = """
             SELECT
                 EXTRACT(QUARTER FROM completed_at) as quarter,
                 COUNT(*) as completion_count
             FROM complete_capsule
             WHERE completed_at >= :startDate AND completed_at <= :endDate
             GROUP BY EXTRACT(QUARTER FROM completed_at)
             ORDER BY quarter
             """, nativeQuery = true)
    List<Object[]> getQuarterlyCompletionCounts(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query(value = """
             SELECT
                 EXTRACT(YEAR FROM completed_at) as year,
                 COUNT(*) as completion_count
             FROM complete_capsule
             WHERE completed_at >= :startDate AND completed_at <= :endDate
             GROUP BY EXTRACT(YEAR FROM completed_at)
             ORDER BY year
             """, nativeQuery = true)
    List<Object[]> getYearlyCompletionCounts(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
