package com.capstone.service;

import com.capstone.dto.response.*;

import java.util.List;

public interface AnalyticsService {

    DashboardSummaryDto getDashboardSummary(String period);

    /**
     * Get performance trends combining assessment scores and capsule completions
     * PERFORMANCE: Optimized to combine two data sources efficiently
     */
    List<PerformanceTrendDto> getPerformanceTrends(String period);

    List<AssessmentScoreDistributionDto> getScoreDistribution(String period);


}
