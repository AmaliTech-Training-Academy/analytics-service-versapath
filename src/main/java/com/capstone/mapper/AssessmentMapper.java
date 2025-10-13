package com.capstone.mapper;

import com.capstone.dto.response.AssessmentResponseDto;
import com.capstone.dto.response.AssessmentSummaryDto;
import com.capstone.model.AssessmentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    @Mapping(source = "userSnapshot.userId", target = "userId")
    @Mapping(source = "userSnapshot.email", target = "userEmail")
    @Mapping(source = "userSnapshot.username", target = "userName")
    @Mapping(source = "capsuleSnapshot.capsuleId", target = "capsuleId")
    @Mapping(source = "capsuleSnapshot.capsuleName", target = "capsuleName")
    AssessmentResponseDto toResponseDto(AssessmentSnapshot entity);

    @Mapping(source = "capsuleSnapshot.capsuleName", target = "capsuleName")
    AssessmentSummaryDto toSummaryDto(AssessmentSnapshot entity);

    List<AssessmentResponseDto> toResponseDtoList(List<AssessmentSnapshot> entities);

    List<AssessmentSummaryDto> toSummaryDtoList(List<AssessmentSnapshot> entities);
}
