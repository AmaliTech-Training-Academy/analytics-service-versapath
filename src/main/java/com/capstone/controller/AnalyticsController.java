package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.AssessmentScoreDistributionDto;
import com.capstone.dto.response.DashboardSummaryDto;
import com.capstone.dto.response.PerformanceTrendDto;
import com.capstone.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Analytics", description = "Analytics and reporting endpoints for dashboard")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/summary")
    @Operation(
            summary = "Get dashboard summary statistics",
            description = "Returns aggregated metrics including average performance, career ready count, and skills verified count"
    )
    public ResponseEntity<ApiResponseDto<DashboardSummaryDto>> getDashboardSummary(
            @Parameter(description = "Time period for analysis", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            @Pattern(regexp = "^(monthly|quarterly|yearly)$", message = "Period must be 'monthly', 'quarterly', or 'yearly'")
            String period) {

            log.info("Fetching dashboard summary for period: {}", period);

            DashboardSummaryDto summary = analyticsService.getDashboardSummary(period);

            log.info("Successfully retrieved dashboard summary for period: {} - avg performance: {}, career ready: {}, skills verified: {}",
                    period, summary.getAveragePerformance(), summary.getCareerReady(), summary.getSkillsVerified());

            return ResponseEntity.ok(ApiResponseDto.success(summary, "Dashboard summary retrieved successfully"));

    }

    @GetMapping("/performance-trends")
    @Operation(
            summary = "Get performance trends data",
            description = "Returns time-series data combining assessment scores and capsule completions for trend charts"
    )
    public ResponseEntity<ApiResponseDto<List<PerformanceTrendDto>>> getPerformanceTrends(
            @Parameter(description = "Time period for trend analysis", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            @Pattern(regexp = "^(monthly|quarterly|yearly)$", message = "Period must be 'monthly', 'quarterly', or 'yearly'")
            String period) {

            log.info("Fetching performance trends for period: {}", period);

            List<PerformanceTrendDto> trends = analyticsService.getPerformanceTrends(period);

            log.info("Successfully retrieved {} performance trend data points for period: {}", trends.size(), period);

            return ResponseEntity.ok(ApiResponseDto.success(trends, "Performance trends retrieved successfully"));

    }

    @GetMapping("/score-distribution")
    @Operation(
            summary = "Get assessment score distribution",
            description = "Returns learner counts grouped by score ranges for distribution charts"
    )
    public ResponseEntity<ApiResponseDto<List<AssessmentScoreDistributionDto>>> getScoreDistribution(
            @Parameter(description = "Time period for score analysis", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            @Pattern(regexp = "^(monthly|quarterly|yearly)$", message = "Period must be 'monthly', 'quarterly', or 'yearly'")
            String period) {

        log.info("Fetching score distribution for period: {}", period);

            List<AssessmentScoreDistributionDto> distribution = analyticsService.getScoreDistribution(period);

            long totalLearners = distribution.stream().mapToLong(AssessmentScoreDistributionDto::getCount).sum();
            log.info("Successfully retrieved score distribution for period: {} - total learners: {}", period, totalLearners);

            return ResponseEntity.ok(ApiResponseDto.success(distribution, "Score distribution retrieved successfully"));

    }
}
