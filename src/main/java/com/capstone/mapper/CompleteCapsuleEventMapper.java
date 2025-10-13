package com.capstone.mapper;

import com.capstone.model.CompleteCapsule;
import org.common.event.CapsuleCompletionEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompleteCapsuleEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userSnapshot", ignore = true)
    @Mapping(target = "capsuleSnapshot", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CompleteCapsule toEntity(CapsuleCompletionEvent event);
}
