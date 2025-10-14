package com.capstone.service;

import com.capstone.dto.response.AssessmentResponseDto;
import com.capstone.dto.response.AssessmentSummaryDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.model.AssessmentSnapshot;
import org.common.event.AssessmentResultEvent;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentSnapshotService {

    AssessmentSnapshot processAssessmentEvent(AssessmentResultEvent event);

    AssessmentSnapshot createAssessment(AssessmentResultEvent event);

    AssessmentSnapshot updateAssessment(AssessmentSnapshot existingAssessment, AssessmentResultEvent event);

    Optional<AssessmentSnapshot> findByUserAssessmentAttempt(UUID userId, UUID assessmentId, Integer attemptNumber);

    PaginatedResponseDto<AssessmentResponseDto> getUserAssessments(UUID userId, Pageable pageable);

    PaginatedResponseDto<AssessmentSummaryDto> getCapsuleAssessments(UUID capsuleId, Pageable pageable);
}
