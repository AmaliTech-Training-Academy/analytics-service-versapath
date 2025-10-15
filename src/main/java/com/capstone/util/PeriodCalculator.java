package com.capstone.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling different time periods in analytics
 */
@Component
@Slf4j
public class PeriodCalculator {

    /**
     * Date range container for database queries
     */
    @Data
    public static class DateRange {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        public DateRange(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;

            // Validation to prevent invalid ranges that could cause performance issues
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        }
    }

    /**
     * Get date range for the specified period
     */
    public DateRange getPeriodRange(String period) {
        return switch (period.toLowerCase()) {
            case "monthly", "quarterly" -> getCurrentYearRange();
            case "yearly" -> getMultiYearRange();
            default -> {
                log.warn("Unknown period '{}', defaulting to monthly", period);
                yield getCurrentYearRange();
            }
        };
    }

    /**
     * Get current year range (January 1 to December 31)
     * PERFORMANCE: Uses year boundaries for optimal index usage
     */
    public DateRange getCurrentYearRange() {
        int currentYear = Year.now().getValue();

        LocalDateTime startOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        return new DateRange(startOfYear, endOfYear);
    }

    /**
     * Get multi-year range for yearly analytics (last 3 years)
     * PERFORMANCE: Limited to 3 years to prevent excessive data processing
     */
    public DateRange getMultiYearRange() {
        int currentYear = Year.now().getValue();

        // Start from 2 years ago to get 3-year trend
        LocalDateTime startDate = LocalDateTime.of(currentYear - 2, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        return new DateRange(startDate, endDate);
    }

    /**
     * Get custom date range for specific requirements
     * PERFORMANCE: Allows precise control over query scope
     */
    public DateRange getCustomRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return new DateRange(start, end);
    }

    /**
     * Get period labels for frontend consumption
     * PERFORMANCE: Pre-computed static lists to avoid repeated calculations
     */
    public List<String> getPeriodLabels(String period) {
        return switch (period.toLowerCase()) {
            case "monthly" -> getMonthLabels();
            case "quarterly" -> getQuarterLabels();
            case "yearly" -> getYearLabels();
            default -> {
                log.warn("Unknown period '{}', defaulting to monthly labels", period);
                yield getMonthLabels();
            }
        };
    }

    /**
     * Get month labels (Jan, Feb, Mar...)
     */
    public List<String> getMonthLabels() {
        return Arrays.asList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        );
    }

    /**
     * Get quarter labels (Q1, Q2, Q3, Q4)
     */
    public List<String> getQuarterLabels() {
        return Arrays.asList("Q1", "Q2", "Q3", "Q4");
    }

    /**
     * Get year labels for multi-year analytics
     * PERFORMANCE: Limited to last 3 years to prevent excessive processing
     */
    public List<String> getYearLabels() {
        int currentYear = Year.now().getValue();
        return Arrays.asList(
                String.valueOf(currentYear - 2),
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear)
        );
    }

    /**
     * Convert month number (1-12) to month label
     * PERFORMANCE: Direct array access O(1) lookup
     */
    public String getMonthLabel(int monthNumber) {
        if (monthNumber < 1 || monthNumber > 12) {
            log.warn("Invalid month number: {}, returning 'Unknown'", monthNumber);
            return "Unknown";
        }

        return getMonthLabels().get(monthNumber - 1); // Convert to 0-based index
    }

    /**
     * Get all months in the current year with data availability check
     */
    public List<Integer> getAvailableMonths() {
        int currentMonth = YearMonth.now().getMonthValue();
        List<Integer> months = new ArrayList<>();

        // Only include months up to current month to avoid empty data queries
        for (int month = 1; month <= currentMonth; month++) {
            months.add(month);
        }

        return months;
    }

    /**
     * Check if a period is valid for analytics
     * PERFORMANCE: Early validation to prevent unnecessary processing
     */
    public boolean isValidPeriod(String period) {
        if (period == null || period.trim().isEmpty()) {
            return false;
        }

        String normalizedPeriod = period.toLowerCase().trim();
        return normalizedPeriod.equals("monthly") ||
                normalizedPeriod.equals("quarterly") ||
                normalizedPeriod.equals("yearly");
    }

    /**
     * Get optimal chunk size for period-based processing
     * PERFORMANCE: Returns appropriate batch sizes based on period granularity
     *
     * @param period The analytics period
     * @return Suggested batch size for processing
     */
    public int getOptimalChunkSize(String period) {
        return switch (period.toLowerCase()) {
            case "quarterly" -> 4;
            case "yearly" -> 3;
            default -> 12;
        };
    }
}
