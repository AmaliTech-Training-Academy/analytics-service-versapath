package com.capstone.service.mapping;

import com.capstone.model.CapsuleSnapshot;

import java.util.List;

/**
 * Generic interface for smart updating capsule mappings
 * @param <T> The target entity type (AtomSnapshot or ClusterSnapshot)
 * @param <M> The mapping entity type (CapsuleAtomMapping or CapsuleClusterMapping)
 * @param <D> The input data type for the mapping
 */
public interface MappingUpdater<T, M, D> {

    /**
     * Smart update mappings for a capsule
     * @param capsule The capsule to update mappings for
     * @param mappingData The new mapping data from the event
     */
    void smartUpdateMappings(CapsuleSnapshot capsule, List<D> mappingData);

    /**
     * Verify and parse the input mapping data
     * @param mappingData Raw mapping data from event
     * @return Parsed and verified mappings
     */
    List<ParsedMapping<T>> verifyAndParse(List<D> mappingData);

    /**
     * Analyze differences between existing and new mappings
     * @param capsule The capsule containing existing mappings
     * @param newMappings The new parsed mappings
     * @return Analysis of what needs to be added, updated, or preserved
     */
    MappingUpdateAnalysis<M> analyzeChanges(CapsuleSnapshot capsule, List<ParsedMapping<T>> newMappings);

    /**
     * Apply the analyzed updates to the capsule
     * @param capsule The capsule to update
     * @param analysis The analysis of changes to apply
     */
    void applyUpdates(CapsuleSnapshot capsule, MappingUpdateAnalysis<M> analysis);
}
