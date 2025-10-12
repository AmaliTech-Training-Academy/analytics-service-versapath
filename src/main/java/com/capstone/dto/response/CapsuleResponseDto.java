package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapsuleResponseDto {
    private UUID id;
    private UUID capsuleId;
    private String capsuleName;
    private String description;
    private String difficultyLevel;
    private String proficiencyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalAtoms;
    private Integer totalClusters;
    private List<AtomSummaryDto> atoms;
    private List<ClusterSummaryDto> clusters;
}
