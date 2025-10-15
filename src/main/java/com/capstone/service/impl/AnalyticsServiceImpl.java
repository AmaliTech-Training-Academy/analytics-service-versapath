package com.capstone.service.impl;

import com.capstone.dto.response.*;
import com.capstone.repository.AssessmentSnapshotRepository;
import com.capstone.repository.CompleteCapsuleRepository;
import com.capstone.service.AnalyticsService;
import com.capstone.util.PeriodCalculator;
import com.capstone.util.PeriodCalculator.DateRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AssessmentSnapshotRepository assessmentRepository;
    private final CompleteCapsuleRepository completeCapsuleRepository;
    private final PeriodCalculator periodCalculator;


    @Override
    public DashboardSummaryDto getDashboardSummary(String period) {
        log.debug("Getting dashboard summary for period: {}", period);

        long startTime = System.currentTimeMillis();

        try {
            // Validate period and get date range
            if (!periodCalculator.isValidPeriod(period)) {
                throw new IllegalArgumentException("Invalid period: " + period);
            }

            DateRange dateRange = periodCalculator.getPeriodRange(period);

            // Get assessment statistics - single query for performance
            List<Object[]> assessmentStatsList = assessmentRepository.getDashboardSummaryStats(
                    dateRange.getStartDate(), dateRange.getEndDate());

            // Get capsule completion count - single query
            Long skillsVerified = completeCapsuleRepository.countCompletedCapsulesInPeriod(
                    dateRange.getStartDate(), dateRange.getEndDate());

            // Extract values with null safety
            Double averagePerformance = 0.0;
            Long totalAssessments = 0L;
            Long passingAssessments = 0L;

            if (assessmentStatsList != null && !assessmentStatsList.isEmpty()) {
                Object[] assessmentStats = assessmentStatsList.getFirst();
                averagePerformance = extractDoubleValue(assessmentStats);
                totalAssessments = extractLongValue(assessmentStats, 1);
                passingAssessments = extractLongValue(assessmentStats, 2);
            }

            // Calculate career ready count (users with 70%+ average)
            Long careerReady = assessmentRepository.countUsersWithHighPerformance(
                    dateRange.getStartDate(), dateRange.getEndDate(), 70.0);

            DashboardSummaryDto result = DashboardSummaryDto.builder()
                    .averagePerformance(averagePerformance)
                    .careerReady(careerReady)
                    .skillsVerified(skillsVerified)
                    .totalAssessments(totalAssessments)
                    .passingRate(calculatePassingRate(passingAssessments, totalAssessments))
                    .build();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Dashboard summary generated in {}ms for period: {}", executionTime, period);

            return result;

        } catch (Exception e) {
            log.error("Error generating dashboard summary for period: {}", period, e);
            throw new RuntimeException("Failed to generate dashboard summary", e);
        }
    }


    @Override
    public List<PerformanceTrendDto> getPerformanceTrends(String period) {
        log.debug("Getting performance trends for period: {}", period);

        long startTime = System.currentTimeMillis();

        try {
            DateRange dateRange = periodCalculator.getPeriodRange(period);
            List<String> periodLabels = periodCalculator.getPeriodLabels(period);

            // PHASE 1: Get engaged learner counts by month/quarter/year
            Map<Integer, Long> engagedLearners = getEngagedLearnersByPeriod(dateRange, period);
            log.debug("Retrieved engaged learners for {} periods", engagedLearners.size());

            // PHASE 2: Get capsule completion counts by month/quarter/year
            Map<Integer, Long> capsuleCompletions = getCapsuleCompletionsByPeriod(dateRange, period);
            log.debug("Retrieved capsule completions for {} periods", capsuleCompletions.size());

            // PHASE 3: Merge learner and completion data efficiently
            List<PerformanceTrendDto> trends = mergePerformanceData(
                    engagedLearners, capsuleCompletions, periodLabels);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Performance trends generated in {}ms for period: {} ({} data points)",
                    executionTime, period, trends.size());

            return trends;

        } catch (Exception e) {
            log.error("Error generating performance trends for period: {}", period, e);
            throw new RuntimeException("Failed to generate performance trends", e);
        }
    }


    @Override
    public List<AssessmentScoreDistributionDto> getScoreDistribution(String period) {
        log.debug("Getting score distribution for period: {}", period);

        long startTime = System.currentTimeMillis();

        try {
            DateRange dateRange = periodCalculator.getPeriodRange(period);

            // Get raw score distribution data - optimized single query
            List<Object[]> distributionData = assessmentRepository.getScoreDistributionData(
                    dateRange.getStartDate(), dateRange.getEndDate());

            // Convert to DTOs with predefined score ranges
            List<AssessmentScoreDistributionDto> distribution = buildScoreDistribution(distributionData);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Score distribution generated in {}ms for period: {} ({} ranges)",
                    executionTime, period, distribution.size());

            return distribution;

        } catch (Exception e) {
            log.error("Error generating score distribution for period: {}", period, e);
            throw new RuntimeException("Failed to generate score distribution", e);
        }
    }

    /**
     * Get engaged learner counts grouped by time period
     */
    private Map<Integer, Long> getEngagedLearnersByPeriod(DateRange dateRange, String period) {
        List<Object[]> rawData;

        switch (period.toLowerCase()) {
            case "monthly" -> rawData = assessmentRepository.getMonthlyEngagedLearners(
                    dateRange.getStartDate(), dateRange.getEndDate());
            case "quarterly" -> rawData = assessmentRepository.getQuarterlyEngagedLearners(
                    dateRange.getStartDate(), dateRange.getEndDate());
            case "yearly" -> rawData = assessmentRepository.getYearlyEngagedLearners(
                    dateRange.getStartDate(), dateRange.getEndDate());
            default -> {
                log.warn("Unknown period for engaged learners: {}, defaulting to monthly", period);
                rawData = assessmentRepository.getMonthlyEngagedLearners(
                        dateRange.getStartDate(), dateRange.getEndDate());
            }
        }

        // Convert to Map for O(1) lookup during merge
        return rawData.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(), // period number
                        row -> row[1] != null ? ((Number) row[1]).longValue() : 0L, // learner count
                        (existing, replacement) -> existing // handle duplicates
                ));
    }

    /**
     * Get capsule completion counts grouped by time period
     */
    private Map<Integer, Long> getCapsuleCompletionsByPeriod(DateRange dateRange, String period) {
        List<Object[]> rawData;

        switch (period.toLowerCase()) {
            case "monthly" -> rawData = completeCapsuleRepository.getMonthlyCompletionCounts(
                    dateRange.getStartDate(), dateRange.getEndDate());
            case "quarterly" -> rawData = completeCapsuleRepository.getQuarterlyCompletionCounts(
                    dateRange.getStartDate(), dateRange.getEndDate());
            case "yearly" -> rawData = completeCapsuleRepository.getYearlyCompletionCounts(
                    dateRange.getStartDate(), dateRange.getEndDate());
            default -> {
                log.warn("Unknown period for completions: {}, defaulting to monthly", period);
                rawData = completeCapsuleRepository.getMonthlyCompletionCounts(
                        dateRange.getStartDate(), dateRange.getEndDate());
            }
        }

        return rawData.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> row[1] != null ? ((Number) row[1]).longValue() : 0L,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Merge assessment and capsule data into performance trends
     */
    private List<PerformanceTrendDto> mergePerformanceData(
            Map<Integer, Long> engagedLearners,
            Map<Integer, Long> capsuleCompletions,
            List<String> periodLabels) {

        List<PerformanceTrendDto> trends = new ArrayList<>(periodLabels.size());

        // Iterate through all expected periods to ensure complete data set
        for (int i = 0; i < periodLabels.size(); i++) {
            int periodNumber = i + 1;
            String label = periodLabels.get(i);

            // Get data with null safety - default to 0 if no data for period
            Long learners = engagedLearners.getOrDefault(periodNumber, 0L);
            Long completions = capsuleCompletions.getOrDefault(periodNumber, 0L);

            PerformanceTrendDto trend = PerformanceTrendDto.builder()
                    .label(label)
                    .engagedLearners(learners)
                    .capsulesCompleted(completions)
                    .build();

            trends.add(trend);
        }

        return trends;
    }

    /**
     * Build score distribution from raw database results
     */
    private List<AssessmentScoreDistributionDto> buildScoreDistribution(List<Object[]> rawData) {
        // Initialize all score ranges to ensure complete data set
        Map<String, Long> distributionMap = new LinkedHashMap<>();
        distributionMap.put("90-100", 0L);
        distributionMap.put("80-89", 0L);
        distributionMap.put("70-79", 0L);
        distributionMap.put("60-69", 0L);
        distributionMap.put("<60", 0L);

        // Populate with actual data
        for (Object[] row : rawData) {
            String scoreRange = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            distributionMap.put(scoreRange, count);
        }

        // Convert to DTO list maintaining order
        return distributionMap.entrySet().stream()
                .map(entry -> AssessmentScoreDistributionDto.builder()
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }


    /**
     * Safely extract Double value from Object array with default fallback
     */
    private Double extractDoubleValue(Object[] array) {
        if (array == null || array.length == 0 || array[0] == null) {
            return 0.0;
        }
        return ((Number) array[0]).doubleValue();
    }

    /**
     * Safely extract Long value from Object array with default fallback
     */
    private Long extractLongValue(Object[] array, int index) {
        if (array == null || array.length <= index || array[index] == null) {
            return 0L;
        }
        return ((Number) array[index]).longValue();
    }

    /**
     * Calculate passing rate percentage with division by zero protection
     */
    private Double calculatePassingRate(Long passingCount, Long totalCount) {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }

        if (passingCount == null) {
            return 0.0;
        }

        return (passingCount.doubleValue() / totalCount.doubleValue()) * 100.0;
    }

}
