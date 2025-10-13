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
public class UserAssessmentStatsDto {
    private UUID userId;
    private Long totalAssessments;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Long passedAssessments;
    private Long failedAssessments;
    private Double passRate;
}
