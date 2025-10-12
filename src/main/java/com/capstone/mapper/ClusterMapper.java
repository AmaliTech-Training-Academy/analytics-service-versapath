package com.capstone.mapper;

import com.capstone.dto.response.ClusterResponseDto;
import com.capstone.model.ClusterSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    ClusterResponseDto toResponseDto(ClusterSnapshot entity);
}
