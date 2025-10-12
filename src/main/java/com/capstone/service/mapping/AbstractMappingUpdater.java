package com.capstone.service.mapping;

import com.capstone.model.CapsuleSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Abstract base class providing common smart update logic
 * @param <T> The target entity type (AtomSnapshot or ClusterSnapshot)
 * @param <M> The mapping entity type (CapsuleAtomMapping or CapsuleClusterMapping)
 * @param <D> The input data type for the mapping
 */
@Slf4j
public abstract class AbstractMappingUpdater<T, M, D> implements MappingUpdater<T, M, D> {

    @Override
    public void smartUpdateMappings(CapsuleSnapshot capsule, List<D> mappingData) {
        log.debug("Smart updating {} mappings for capsule: {}", getEntityTypeName(), capsule.getCapsuleId());

        try {
            // PHASE 1: VERIFY AND PARSE - Fail Fast Strategy
            List<ParsedMapping<T>> newMappings = verifyAndParse(mappingData);
            log.debug("Verified {} {} mappings for capsule {}",
                    newMappings.size(), getEntityTypeName(), capsule.getCapsuleId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            MappingUpdateAnalysis<M> analysis = analyzeChanges(capsule, newMappings);
            log.debug("Analysis for capsule {}: {} to add, {} to update",
                    capsule.getCapsuleId(), analysis.getToAdd().size(), analysis.getToUpdate().size());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyUpdates(capsule, analysis);

            log.info("Smart {} update completed for capsule {}: {} added, {} updated, {} preserved",
                    getEntityTypeName(), capsule.getCapsuleId(), analysis.getToAdd().size(),
                    analysis.getToUpdate().size(), analysis.getPreserved());

        } catch (Exception e) {
            log.error("Smart {} update failed for capsule {}: {}",
                    getEntityTypeName(), capsule.getCapsuleId(), e.getMessage(), e);
            throw getMappingException(e);
        }
    }

    protected abstract String getEntityTypeName();

    protected abstract RuntimeException getMappingException(Throwable cause);

}
