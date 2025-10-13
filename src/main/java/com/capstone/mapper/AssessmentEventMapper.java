package com.capstone.mapper;

import com.capstone.model.AssessmentSnapshot;
import org.common.event.AssessmentResultEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AssessmentEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userSnapshot", ignore = true)
    @Mapping(target = "capsuleSnapshot", ignore = true)
    @Mapping(source = "grade", target = "score")
    AssessmentSnapshot toAssessmentSnapshot(AssessmentResultEvent event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userSnapshot", ignore = true)
    @Mapping(target = "capsuleSnapshot", ignore = true)
    @Mapping(source = "grade", target = "score")
    void updateAssessmentSnapshot(AssessmentResultEvent event, @MappingTarget AssessmentSnapshot assessmentSnapshot);
}
