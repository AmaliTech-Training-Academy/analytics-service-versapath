package com.capstone.mapper;

import com.capstone.model.CapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CapsuleEventMapper {
    @Mapping(source = "id", target = "capsuleId")
    @Mapping(source = "name", target = "capsuleName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "difficultyLevel", source = "difficulty")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completeCapsules", ignore = true)
    CapsuleSnapshot toCapsuleSnapshot(SkillCapsuleEvent event);

    @Mapping(source = "name", target = "capsuleName")
    @Mapping(target = "difficultyLevel", source = "difficulty")
    @Mapping(target = "capsuleId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completeCapsules", ignore = true)
    void updateCapsuleSnapshot(SkillCapsuleEvent event, @MappingTarget CapsuleSnapshot capsuleSnapshot);
}
