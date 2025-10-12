package com.capstone.mapper;

import com.capstone.dto.response.AtomResponseDto;
import com.capstone.model.AtomSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AtomMapper {

    AtomResponseDto toResponseDto(AtomSnapshot entity);
}
