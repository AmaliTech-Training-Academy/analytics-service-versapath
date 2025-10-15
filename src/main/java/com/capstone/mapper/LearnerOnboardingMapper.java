package com.capstone.mapper;

import com.capstone.model.LearnerOnboarding;
import org.common.event.LearnerOnBoardingEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LearnerOnboardingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "learner", ignore = true)
    @Mapping(target = "talentRoute", ignore = true)
    @Mapping(target = "growthTrack", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    LearnerOnboarding toEntity(LearnerOnBoardingEvent requestDto);
}
