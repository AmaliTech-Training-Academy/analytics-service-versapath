package com.capstone.service.mapping;

import com.capstone.exception.ClusterNotFoundException;
import com.capstone.exception.CapsuleClusterMappingException;
import com.capstone.model.ClusterSnapshot;
import com.capstone.model.CapsuleClusterMapping;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.repository.ClusterSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cluster mapping updater implementation
 */
@Component
@RequiredArgsConstructor
public class ClusterMappingUpdater extends AbstractMappingUpdater<ClusterSnapshot, CapsuleClusterMapping, UUID> {

    private final ClusterSnapshotRepository clusterSnapshotRepository;

    @Override
    public List<ParsedMapping<ClusterSnapshot>> verifyAndParse(List<UUID> mappingData) {
        List<ParsedMapping<ClusterSnapshot>> parsedMappings = new ArrayList<>();

        for (UUID clusterId : mappingData) {
            // Verify cluster exists
            ClusterSnapshot cluster = clusterSnapshotRepository.findByClusterId(clusterId)
                    .orElseThrow(() -> new ClusterNotFoundException("Cluster not found with ID: " + clusterId));

            parsedMappings.add(new ParsedMapping<>(cluster)); // No sequence for clusters
        }

        return parsedMappings;
    }

    @Override
    public MappingUpdateAnalysis<CapsuleClusterMapping> analyzeChanges(CapsuleSnapshot capsule,
                                                                       List<ParsedMapping<ClusterSnapshot>> newMappings) {
        // Build lookup map of existing mappings
        Map<UUID, CapsuleClusterMapping> existingMap = capsule.getCapsuleClusterMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getTheCluster().getClusterId(),
                        mapping -> mapping
                ));

        List<CapsuleClusterMapping> toAdd = new ArrayList<>();
        List<CapsuleClusterMapping> toUpdate = new ArrayList<>(); // Will always be empty for clusters
        int preserved = 0;

        // Process each new mapping
        for (ParsedMapping<ClusterSnapshot> newMapping : newMappings) {
            UUID clusterId = newMapping.getEntity().getClusterId();

            if (existingMap.containsKey(clusterId)) {
                // CLUSTER EXISTS - No updates needed (no sequence field)
                preserved++;
                existingMap.remove(clusterId);
            } else {
                // NEW CLUSTER - Create mapping
                CapsuleClusterMapping newMappingEntity = CapsuleClusterMapping.builder()
                        .skillCapsule(capsule)
                        .theCluster(newMapping.getEntity())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new MappingUpdateAnalysis<>(toAdd, toUpdate, preserved);
    }

    @Override
    public void applyUpdates(CapsuleSnapshot capsule, MappingUpdateAnalysis<CapsuleClusterMapping> analysis) {
        if (!analysis.getToAdd().isEmpty()) {
            capsule.getCapsuleClusterMappings().addAll(analysis.getToAdd());
        }
    }

    @Override
    protected String getEntityTypeName() {
        return "cluster";
    }

    @Override
    protected RuntimeException getMappingException(Throwable cause) {
        return new CapsuleClusterMappingException("Smart update failed", cause);
    }
}
