package com.capstone.service.mapping;

import com.capstone.exception.AtomNotFoundException;
import com.capstone.exception.CapsuleAtomMappingException;
import com.capstone.model.AtomSnapshot;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.repository.AtomSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Atom mapping updater implementation
 */
@Component
@RequiredArgsConstructor
public class AtomMappingUpdater extends AbstractMappingUpdater<AtomSnapshot, CapsuleAtomMapping, Map<UUID, Integer>> {

    private final AtomSnapshotRepository atomSnapshotRepository;

    @Override
    public List<ParsedMapping<AtomSnapshot>> verifyAndParse(List<Map<UUID, Integer>> mappingData) {
        List<ParsedMapping<AtomSnapshot>> parsedMappings = new ArrayList<>();

        for (Map<UUID, Integer> atomMap : mappingData) {
            for (Map.Entry<UUID, Integer> entry : atomMap.entrySet()) {
                UUID atomId = entry.getKey();
                Integer sequence = entry.getValue();

                // Verify atom exists
                AtomSnapshot atom = atomSnapshotRepository.findByAtomId(atomId)
                        .orElseThrow(() -> new AtomNotFoundException("Atom not found with ID: " + atomId));

                // Validate sequence order
                if (sequence == null || sequence < 1) {
                    throw new CapsuleAtomMappingException("Invalid sequence order: " + sequence);
                }

                parsedMappings.add(new ParsedMapping<>(atom, sequence));
            }
        }

        return parsedMappings;
    }

    @Override
    public MappingUpdateAnalysis<CapsuleAtomMapping> analyzeChanges(CapsuleSnapshot capsule,
                                                                    List<ParsedMapping<AtomSnapshot>> newMappings) {
        // Build lookup map of existing mappings
        Map<UUID, CapsuleAtomMapping> existingMap = capsule.getCapsuleAtomMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getSkillAtom().getAtomId(),
                        mapping -> mapping
                ));

        List<CapsuleAtomMapping> toAdd = new ArrayList<>();
        List<CapsuleAtomMapping> toUpdate = new ArrayList<>();
        int preserved = 0;

        // Process each new mapping
        for (ParsedMapping<AtomSnapshot> newMapping : newMappings) {
            UUID atomId = newMapping.getEntity().getAtomId();

            if (existingMap.containsKey(atomId)) {
                // ATOM EXISTS - Check if sequence changed
                CapsuleAtomMapping existing = existingMap.get(atomId);
                if (!existing.getSequenceOrder().equals(newMapping.getSequence())) {
                    existing.setSequenceOrder(newMapping.getSequence());
                    toUpdate.add(existing);
                } else {
                    preserved++; // No change needed
                }
                // Remove from map to track what remains
                existingMap.remove(atomId);
            } else {
                // NEW ATOM - Create mapping
                CapsuleAtomMapping newMappingEntity = CapsuleAtomMapping.builder()
                        .skillCapsule(capsule)
                        .skillAtom(newMapping.getEntity())
                        .sequenceOrder(newMapping.getSequence())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new MappingUpdateAnalysis<>(toAdd, toUpdate, preserved);
    }

    @Override
    public void applyUpdates(CapsuleSnapshot capsule, MappingUpdateAnalysis<CapsuleAtomMapping> analysis) {
        if (!analysis.getToAdd().isEmpty()) {
            capsule.getCapsuleAtomMappings().addAll(analysis.getToAdd());
        }
    }

    @Override
    protected String getEntityTypeName() {
        return "atom";
    }

    @Override
    protected RuntimeException getMappingException(Throwable cause) {
        return new CapsuleAtomMappingException("Smart update failed", cause);
    }
}
