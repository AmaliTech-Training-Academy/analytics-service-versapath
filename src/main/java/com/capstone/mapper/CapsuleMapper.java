package com.capstone.mapper;

import com.capstone.dto.response.AtomSummaryDto;
import com.capstone.dto.response.CapsuleResponseDto;
import com.capstone.dto.response.ClusterSummaryDto;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.CapsuleClusterMapping;
import com.capstone.model.CapsuleSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapsuleMapper {

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "totalClusters", expression = "java(entity.getCapsuleClusterMappings().size())")
    @Mapping(target = "atoms", ignore = true)
    @Mapping(target = "clusters", ignore = true)
    CapsuleResponseDto toBasicResponseDto(CapsuleSnapshot entity);

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "totalClusters", expression = "java(entity.getCapsuleClusterMappings().size())")
    @Mapping(target = "atoms", source = "capsuleAtomMappings")
    @Mapping(target = "clusters", source = "capsuleClusterMappings")
    CapsuleResponseDto toResponseDtoWithAtoms(CapsuleSnapshot entity);

    @Mapping(source = "skillAtom.id", target = "id")
    @Mapping(source = "skillAtom.atomId", target = "atomId")
    @Mapping(source = "skillAtom.name", target = "atomName")
    @Mapping(source = "skillAtom.description", target = "description")
    AtomSummaryDto toSkillAtomSummaryDto(CapsuleAtomMapping mapping);

    @Mapping(source = "theCluster.id", target = "id")
    @Mapping(source = "theCluster.clusterId", target = "clusterId")
    @Mapping(source = "theCluster.name", target = "clusterName")
    @Mapping(source = "theCluster.description", target = "description")
    ClusterSummaryDto toClusterSummaryDto(CapsuleClusterMapping mapping);

    List<AtomSummaryDto> toSkillAtomSummaryDtoList(List<CapsuleAtomMapping> mappings);

    List<ClusterSummaryDto> toClusterSummaryDtoList(List<CapsuleClusterMapping> mappings);
}
