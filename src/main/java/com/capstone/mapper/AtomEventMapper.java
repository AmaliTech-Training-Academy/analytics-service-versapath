package com.capstone.mapper;

import com.capstone.model.AtomSnapshot;
import org.common.event.SkillAtomEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AtomEventMapper {

    @Mapping(source = "id", target = "atomId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AtomSnapshot toAtomSnapshot(SkillAtomEvent event);

    @Mapping(target = "atomId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAtomSnapshot(SkillAtomEvent event, @MappingTarget AtomSnapshot skillAtomSnapshot);
}
