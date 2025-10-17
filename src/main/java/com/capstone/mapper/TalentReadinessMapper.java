package com.capstone.mapper;

import com.capstone.dto.response.ClusterDto;
import com.capstone.dto.response.TalentReadinessDto;
import com.capstone.model.ClusterSnapshot;
import com.capstone.model.LearnerOnboarding;
import com.capstone.model.UserSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentReadinessMapper {

    @Mapping(source = "clusterId", target = "clusterId")
    @Mapping(source = "name", target = "clusterName")
    ClusterDto toClusterDto(ClusterSnapshot clusterSnapshot);

    List<ClusterDto> toClusterDtoList(List<ClusterSnapshot> clusterSnapshots);

    @Mapping(source = "onboarding.learner.userId", target = "learnerId")
    @Mapping(source = "onboarding.learner", target = "learnerName", qualifiedByName = "getFullName")
    @Mapping(source = "onboarding.learner.email", target = "learnerEmail")
    @Mapping(source = "onboarding.talentRoute.talentRouteId", target = "talentRouteId")
    @Mapping(source = "onboarding.talentRoute.routeName", target = "talentRouteName")
    @Mapping(source = "onboarding.growthTrack.growthTrackId", target = "growthTrackId")
    @Mapping(source = "onboarding.growthTrack.trackName", target = "growthTrackName")
    @Mapping(target = "assessmentAverage", ignore = true)
    @Mapping(target = "skillsCompleted", ignore = true)
    @Mapping(target = "skillsTotal", ignore = true)
    @Mapping(target = "skillsProgress", ignore = true)
    @Mapping(target = "skillsProgressPercentage", ignore = true)
    @Mapping(target = "clusters", ignore = true)
    @Mapping(target = "readinessLevel", ignore = true)
    @Mapping(target = "readinessScore", ignore = true)
    TalentReadinessDto toBaseTalentReadinessDto(LearnerOnboarding onboarding);

    @Named("getFullName")
    default String getFullName(UserSnapshot userSnapshot) {
        return userSnapshot.getFullName();
    }
}