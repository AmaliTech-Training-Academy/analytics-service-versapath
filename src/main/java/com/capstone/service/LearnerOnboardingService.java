package com.capstone.service;

import com.capstone.model.LearnerOnboarding;
import org.common.event.LearnerOnBoardingEvent;

import java.util.UUID;

public interface LearnerOnboardingService {
    LearnerOnboarding createOnboarding(LearnerOnBoardingEvent event);
    boolean existsByLearnerId(UUID learnerId);
}
