package com.capstone.service.impl;

import com.capstone.exception.CapsuleCompletionProcessingException;
import com.capstone.exception.CapsuleNotFoundException;
import com.capstone.exception.UserProcessingException;
import com.capstone.mapper.CompleteCapsuleEventMapper;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.model.CompleteCapsule;
import com.capstone.model.UserSnapshot;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.repository.CompleteCapsuleRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.CompleteCapsuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.CapsuleCompletionEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompleteCapsuleServiceImpl implements CompleteCapsuleService {

    private final CompleteCapsuleRepository completeCapsuleRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final CompleteCapsuleEventMapper eventMapper;

    @Override
    public CompleteCapsule processCapsuleCompletionEvent(CapsuleCompletionEvent event) {

        log.info("Processing capsule completion event for learnerId: {}, capsuleId: {}",
                event.getLearnerId(), event.getCapsuleId());

        try {
            if (isAlreadyComplete(event.getLearnerId(), event.getCapsuleId())) {
                throw new CapsuleCompletionProcessingException("Capsule already completed");
            }

            validateUserExists(event.getLearnerId());
            validateCapsuleExists(event.getCapsuleId());

            UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(event.getLearnerId())
                    .orElseThrow(() -> new UserProcessingException("User not found with ID: " + event.getLearnerId()));

            CapsuleSnapshot capsuleSnapshot = capsuleSnapshotRepository.findByCapsuleId(event.getCapsuleId())
                    .orElseThrow(() -> new CapsuleNotFoundException("Capsule not found with ID: " + event.getCapsuleId()));

            CompleteCapsule capsule = eventMapper.toEntity(event);
            capsule.setUserSnapshot(userSnapshot);
            capsule.setCapsuleSnapshot(capsuleSnapshot);

            CompleteCapsule savedCapsule= completeCapsuleRepository.save(capsule);
            log.info("Successfully saved completed capsule for learnerId: {} and capsuleId: {}",
                    event.getLearnerId(), event.getCapsuleId());

            return savedCapsule;

        } catch (DataIntegrityViolationException e) {
            throw new CapsuleCompletionProcessingException("Failed to save completed capsule due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Error processing capsule completion event for learnerId: {}, capsuleId: {}",
                    event.getLearnerId(), event.getCapsuleId(), e);
            throw new CapsuleCompletionProcessingException("Failed to process capsule completion event", e);
        }

    }

    @Override
    public boolean isAlreadyComplete(UUID learnerId, UUID capsuleId) {
        return completeCapsuleRepository.existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(learnerId, capsuleId);
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
}
