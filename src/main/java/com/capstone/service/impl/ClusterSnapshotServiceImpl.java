package com.capstone.service.impl;

import com.capstone.exception.ClusterProcessingException;
import com.capstone.mapper.ClusterEventMapper;
import com.capstone.model.ClusterSnapshot;
import com.capstone.repository.ClusterSnapshotRepository;
import com.capstone.service.ClusterSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ClusterEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClusterSnapshotServiceImpl implements ClusterSnapshotService {

    private final ClusterSnapshotRepository clusterSnapshotRepository;
    private final ClusterEventMapper clusterEventMapper;


    @Override
    public ClusterSnapshot processClusterEvent(ClusterEvent event) {
        log.info("Processing Cluster event for ClusterId: {}", event.getClusterId());

        try {
            Optional<ClusterSnapshot> existingCluster = clusterSnapshotRepository.findByClusterId(event.getClusterId());

            if (existingCluster.isPresent()) {
                log.info("Cluster exists, updating Cluster with ID: {}", event.getClusterId());
                return updateCluster(existingCluster.get(), event);
            } else {
                log.info("Cluster does not exist, creating new Cluster with ID: {}", event.getClusterId());
                return createCluster(event);
            }

        } catch (Exception e) {
            log.error("Error processing Cluster event for ClusterId: {}", event.getClusterId(), e);
            throw new ClusterProcessingException("Failed to process Cluster event", e);
        }
    }

    @Override
    public ClusterSnapshot createCluster(ClusterEvent event) {
        log.debug("Creating new Cluster from event: {}", event);

        try {
            ClusterSnapshot newCluster = clusterEventMapper.toClusterSnapshot(event);
            ClusterSnapshot savedCluster = clusterSnapshotRepository.save(newCluster);
            log.info("Successfully created Cluster with ID: {}", savedCluster.getClusterId());
            return savedCluster;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating Cluster with ID: {}", event.getClusterId(), e);
            throw new ClusterProcessingException("Cluster creation failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error creating Cluster with ID: {}", event.getClusterId(), e);
            throw new ClusterProcessingException("Failed to create Cluster", e);
        }
    }

    @Override
    public ClusterSnapshot updateCluster(ClusterSnapshot existingCluster, ClusterEvent event) {
        log.debug("Updating existing  Cluster {} with event data: {}", existingCluster.getClusterId(), event);

        try {
            clusterEventMapper.updateClusterSnapshot(event, existingCluster);
            ClusterSnapshot updatedCluster = clusterSnapshotRepository.save(existingCluster);
            log.info("Successfully updated Cluster with ID: {}", updatedCluster.getClusterId());
            return updatedCluster;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating Cluster with ID: {}", existingCluster.getClusterId(), e);
            throw new ClusterProcessingException(" Cluster update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating Cluster with ID: {}", existingCluster.getClusterId(), e);
            throw new ClusterProcessingException("Failed to update Cluster", e);
        }
    }

    @Override
    public Optional<ClusterSnapshot> findByClusterId(UUID clusterId) {
        log.debug("Finding Cluster by ClusterId: {}", clusterId);
        return clusterSnapshotRepository.findByClusterId(clusterId);
    }
}
