package com.capstone.service.impl;

import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.exception.ValidationException;
import com.capstone.mapper.LearnerOnboardingMapper;
import com.capstone.model.*;
import com.capstone.repository.LearnerOnboardingRepository;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.LearnerOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.LearnerOnBoardingEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearnerOnboardingServiceImpl implements LearnerOnboardingService {

    private final LearnerOnboardingRepository learnerOnboardingRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final LearnerOnboardingMapper learnerOnboardingMapper;

    @Override
    @Transactional
    public LearnerOnboarding createOnboarding(LearnerOnBoardingEvent event) {
        log.info("Creating learner onboarding for learner: {}", event.getLearnerId());

        // Check if onboarding already exists for this learner
        if (existsByLearnerId(event.getLearnerId())) {
            throw new ValidationException("Onboarding already exists for learner: " + event.getLearnerId());
        }

        // Validate and get required entities
        UserSnapshot learner = userSnapshotRepository.findByUserId(event.getLearnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + event.getLearnerId()));

        TalentRouteSnapshot talentRoute = talentRouteSnapshotRepository.findByTalentRouteId(event.getTalentRouteId())
                .orElseThrow(() -> new TalentRouteNotFoundException("Talent route not found with ID: " + event.getTalentRouteId()));

        // Create entity and set relationships
        LearnerOnboarding onboarding = learnerOnboardingMapper.toEntity(event);
        onboarding.setLearner(learner);
        onboarding.setTalentRoute(talentRoute);

        return learnerOnboardingRepository.save(onboarding);
    }

    @Override
    public boolean existsByLearnerId(UUID learnerId) {
        return learnerOnboardingRepository.existsByLearnerId(learnerId);
    }
}
