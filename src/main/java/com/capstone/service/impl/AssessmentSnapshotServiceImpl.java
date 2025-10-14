package com.capstone.service.impl;

import com.capstone.dto.response.AssessmentResponseDto;
import com.capstone.dto.response.AssessmentSummaryDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.exception.AssessmentProcessingException;
import com.capstone.exception.CapsuleNotFoundException;
import com.capstone.exception.DuplicateAssessmentAttemptException;
import com.capstone.exception.UserProcessingException;
import com.capstone.mapper.AssessmentEventMapper;
import com.capstone.mapper.AssessmentMapper;
import com.capstone.model.AssessmentSnapshot;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.model.UserSnapshot;
import com.capstone.repository.AssessmentSnapshotRepository;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.AssessmentSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.AssessmentResultEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AssessmentSnapshotServiceImpl implements AssessmentSnapshotService {

    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final AssessmentEventMapper assessmentEventMapper;
    private final AssessmentMapper assessmentMapper;

    @Value("${AUTO_CREATE_ENTITIES:false}")
    private boolean autoCreateEntities;

    @Override
    public AssessmentSnapshot processAssessmentEvent(AssessmentResultEvent event) {
        log.info("Processing assessment event for userId: {}, assessmentId: {}, attempt: {}",
                event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());

        try {

            // Check if this assessment attempt already exists
            Optional<AssessmentSnapshot> existingAssessment = assessmentSnapshotRepository
                    .findByUserSnapshotUserIdAndAssessmentIdAndAttemptNumber(
                            event.getUserId(),
                            event.getAssessmentId(),
                            event.getAttemptNumber()
                    );

            if (existingAssessment.isPresent()) {
                log.info("Assessment attempt exists, updating assessment for userId: {}, assessmentId: {}, attempt: {}",
                        event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());
                return updateAssessment(existingAssessment.get(), event);
            } else {
                log.info("Assessment attempt does not exist, creating new assessment for userId: {}, assessmentId: {}, attempt: {}",
                        event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());
                return createAssessment(event);
            }

        } catch (Exception e) {
            log.error("Error processing assessment event for userId: {}, assessmentId: {}, attempt: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), e);
            throw new AssessmentProcessingException("Failed to process assessment event", e);
        }
    }

    @Override
    public AssessmentSnapshot createAssessment(AssessmentResultEvent event) {
        log.debug("Creating new assessment from event: {}", event);

        try {
            // Validate that required entities exist
            UserSnapshot userSnapshot = validateAndGetUser(event.getUserId());
            CapsuleSnapshot capsuleSnapshot = validateAndGetCapsule(event.getCapsuleId());

            // Map event to entity
            AssessmentSnapshot newAssessment = assessmentEventMapper.toAssessmentSnapshot(event);

            // Set relationships (ignored in mapper)
            newAssessment.setUserSnapshot(userSnapshot);
            newAssessment.setCapsuleSnapshot(capsuleSnapshot);

            // Save assessment
            AssessmentSnapshot savedAssessment = assessmentSnapshotRepository.save(newAssessment);

            log.info("Successfully created assessment with ID: {} for userId: {}, assessmentId: {}, attempt: {}",
                    savedAssessment.getId(), event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());

            return savedAssessment;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating assessment for userId: {}, assessmentId: {}, attempt: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), e);
            throw new DuplicateAssessmentAttemptException(event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());
        } catch (UserProcessingException | CapsuleNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating assessment for userId: {}, assessmentId: {}, attempt: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), e);
            throw new AssessmentProcessingException("Failed to create assessment", e);
        }
    }

    @Override
    public AssessmentSnapshot updateAssessment(AssessmentSnapshot existingAssessment, AssessmentResultEvent event) {
        log.debug("Updating existing assessment {} with event data", existingAssessment.getId());

        try {
            // Update assessment with new event data
            assessmentEventMapper.updateAssessmentSnapshot(event, existingAssessment);

            // Save updated assessment
            AssessmentSnapshot updatedAssessment = assessmentSnapshotRepository.save(existingAssessment);

            log.info("Successfully updated assessment with ID: {} for userId: {}, assessmentId: {}, attempt: {}",
                    updatedAssessment.getId(), event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());

            return updatedAssessment;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating assessment with ID: {}", existingAssessment.getId(), e);
            throw new AssessmentProcessingException("Assessment update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating assessment with ID: {}", existingAssessment.getId(), e);
            throw new AssessmentProcessingException("Failed to update assessment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssessmentSnapshot> findByUserAssessmentAttempt(UUID userId, UUID assessmentId, Integer attemptNumber) {
        log.debug("Finding assessment by userId: {}, assessmentId: {}, attempt: {}", userId, assessmentId, attemptNumber);
        return assessmentSnapshotRepository.findByUserSnapshotUserIdAndAssessmentIdAndAttemptNumber(
                userId, assessmentId, attemptNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<AssessmentResponseDto> getUserAssessments(UUID userId, Pageable pageable) {
        log.debug("Getting paginated assessments for userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // Validate user exists
            validateUserExists(userId);

            // Get paginated assessments
            Page<AssessmentSnapshot> assessmentPage = assessmentSnapshotRepository
                    .findByUserSnapshotUserId(userId, pageable);

            // Convert to DTOs
            Page<AssessmentResponseDto> responsePage = assessmentPage
                    .map(assessmentMapper::toResponseDto);

            log.debug("Found {} assessments for userId: {} (page {} of {})",
                    responsePage.getNumberOfElements(), userId,
                    responsePage.getNumber() + 1, responsePage.getTotalPages());

            return PaginationUtil.toPaginatedResponse(responsePage);

        } catch (Exception e) {
            log.error("Error getting user assessments for userId: {}", userId, e);
            throw new AssessmentProcessingException("Failed to get user assessments", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<AssessmentSummaryDto> getCapsuleAssessments(UUID capsuleId, Pageable pageable) {
        log.debug("Getting paginated assessments for capsuleId: {}, page: {}, size: {}",
                capsuleId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // Validate capsule exists
            validateCapsuleExists(capsuleId);

            // Get paginated assessments
            Page<AssessmentSnapshot> assessmentPage = assessmentSnapshotRepository
                    .findByCapsuleSnapshotCapsuleId(capsuleId, pageable);

            // Convert to summary DTOs (lighter payload for lists)
            Page<AssessmentSummaryDto> responsePage = assessmentPage
                    .map(assessmentMapper::toSummaryDto);

            log.debug("Found {} assessments for capsuleId: {} (page {} of {})",
                    responsePage.getNumberOfElements(), capsuleId,
                    responsePage.getNumber() + 1, responsePage.getTotalPages());

            return PaginationUtil.toPaginatedResponse(responsePage);

        } catch (Exception e) {
            log.error("Error getting capsule assessments for capsuleId: {}", capsuleId, e);
            throw new AssessmentProcessingException("Failed to get capsule assessments", e);
        }
    }

    // === PRIVATE VALIDATION METHODS ===

    private UserSnapshot validateAndGetUser(UUID userId) {
        Optional<UserSnapshot> user = userSnapshotRepository.findByUserId(userId);

        if (user.isPresent()) {
            return user.get();
        }

        // Development mode: Auto-create missing user
        if (autoCreateEntities) {
            log.warn("Development mode: Auto-creating user with ID: {}", userId);
            return createDummyUser(userId);
        }

        throw new UserProcessingException("User not found with ID: " + userId);
    }

    private CapsuleSnapshot validateAndGetCapsule(UUID capsuleId) {
        Optional<CapsuleSnapshot> capsule = capsuleSnapshotRepository.findByCapsuleId(capsuleId);

        if (capsule.isPresent()) {
            return capsule.get();
        }

        // Development mode: Auto-create missing capsule
        if (autoCreateEntities) {
            log.warn("Development mode: Auto-creating capsule with ID: {}", capsuleId);
            return createDummyCapsule(capsuleId);
        }

        throw new CapsuleNotFoundException("Capsule not found with ID: " + capsuleId);
    }

    private void validateUserExists(UUID userId) {
        if (!userSnapshotRepository.existsByUserId(userId)) {
            throw new UserProcessingException("User not found with ID: " + userId);
        }
    }

    private void validateCapsuleExists(UUID capsuleId) {
        if (!capsuleSnapshotRepository.existsByCapsuleId(capsuleId)) {
            throw new CapsuleNotFoundException("Capsule not found with ID: " + capsuleId);
        }
    }

    // Helpers for testing purposes
    private UserSnapshot createDummyUser(UUID userId) {
        UserSnapshot dummyUser = UserSnapshot.builder()
                .userId(userId)
                .email("dummy-" + userId.toString().substring(0, 8) + "@test.com")
                .username("dummy-user-" + userId.toString().substring(0, 8))
                .firstName("Test")
                .lastName("User")
                .build();

        return userSnapshotRepository.save(dummyUser);
    }

    private CapsuleSnapshot createDummyCapsule(UUID capsuleId) {
        CapsuleSnapshot dummyCapsule = CapsuleSnapshot.builder()
                .capsuleId(capsuleId)
                .capsuleName("Dummy Capsule - " + capsuleId.toString().substring(0, 8))
                .description("Auto-generated dummy capsule for development")
                .difficultyLevel("BEGINNER")
                .proficiencyLevel("BASIC")
                .build();

        return capsuleSnapshotRepository.save(dummyCapsule);
    }
}
