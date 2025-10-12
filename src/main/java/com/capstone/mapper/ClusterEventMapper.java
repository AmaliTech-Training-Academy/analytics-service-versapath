package com.capstone.mapper;

import com.capstone.model.ClusterSnapshot;
import org.common.event.ClusterEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClusterEventMapper {


    @Mapping(target = "name", source = "clusterName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClusterSnapshot toClusterSnapshot(ClusterEvent event);

    @Mapping(target = "clusterId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "clusterName")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClusterSnapshot(ClusterEvent event, @MappingTarget ClusterSnapshot clusterSnapshot);
}
