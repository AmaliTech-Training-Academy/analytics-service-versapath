package com.capstone.dto.response;

import com.capstone.model.ReadinessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentReadinessDto {
    private UUID learnerId;
    private String learnerName;
    private String learnerEmail;

    private UUID talentRouteId;
    private String talentRouteName;

    private UUID growthTrackId;
    private String growthTrackName;

    private Double assessmentAverage;

    private Integer skillsCompleted;
    private Integer skillsTotal;
    private String skillsProgress;
    private Double skillsProgressPercentage;

    private List<ClusterDto> clusters;

    private ReadinessLevel readinessLevel;
    private Double readinessScore;
}
