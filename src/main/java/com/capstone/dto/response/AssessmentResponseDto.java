package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentResponseDto {
    private UUID id;
    private UUID assessmentId;
    private String assessmentName;
    private Integer moodleQuizId;
    private Double score;
    private LocalDateTime timeStart;
    private LocalDateTime timeFinish;
    private Integer attemptNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID userId;
    private String userEmail;
    private String userName;

    private UUID capsuleId;
    private String capsuleName;
}
