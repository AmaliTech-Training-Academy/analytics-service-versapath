package com.capstone.service;

import com.capstone.model.ClusterSnapshot;
import org.common.event.ClusterEvent;

import java.util.Optional;
import java.util.UUID;

public interface ClusterSnapshotService {

    ClusterSnapshot processClusterEvent(ClusterEvent event);
    ClusterSnapshot createCluster(ClusterEvent event);
    ClusterSnapshot updateCluster(ClusterSnapshot existingCluster, ClusterEvent event);
    Optional<ClusterSnapshot> findByClusterId(UUID clusterId);
}
