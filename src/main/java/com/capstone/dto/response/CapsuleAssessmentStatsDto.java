package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapsuleAssessmentStatsDto {
    private UUID capsuleId;
    private String capsuleName;
    private Long totalAssessments;
    private Long uniqueLearners;
    private Double averageScore;
    private Double passRate;
    private Long totalAttempts;
    private Double averageAttempts;
}