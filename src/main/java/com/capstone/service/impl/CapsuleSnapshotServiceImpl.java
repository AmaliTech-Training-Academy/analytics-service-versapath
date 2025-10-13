package com.capstone.service.impl;

import com.capstone.exception.CapsuleAtomMappingException;
import com.capstone.exception.CapsuleNotFoundException;
import com.capstone.exception.CapsuleProcessingException;
import com.capstone.exception.DuplicateCapsuleException;
import com.capstone.mapper.CapsuleEventMapper;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.service.CapsuleSnapshotService;
import com.capstone.service.mapping.AtomMappingUpdater;
import com.capstone.service.mapping.ClusterMappingUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CapsuleSnapshotServiceImpl implements CapsuleSnapshotService {

    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final CapsuleEventMapper capsuleEventMapper;
    private final AtomMappingUpdater atomMappingUpdater;
    private final ClusterMappingUpdater clusterMappingUpdater;

    @Override
    public CapsuleSnapshot processCapsuleEvent(SkillCapsuleEvent event) {
        log.info("Processing skill capsule event for capsuleId: {}", event.getId());

        try {
            Optional<CapsuleSnapshot> existingCapsule = capsuleSnapshotRepository.findByCapsuleId(event.getId());

            if (existingCapsule.isPresent()) {
                log.info("Capsule exists, updating capsule with ID: {}", event.getId());
                return updateCapsule(existingCapsule.get(), event);
            } else {
                log.info("Capsule does not exist, creating new capsule with ID: {}", event.getId());
                return createCapsule(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill capsule event for capsuleId: {}", event.getId(), e);
            throw new CapsuleProcessingException("Failed to process skill capsule event", e);
        }
    }

    @Override
    public CapsuleSnapshot createCapsule(SkillCapsuleEvent event) {
        log.debug("Creating new skill capsule from event: {}", event);

        try {

            CapsuleSnapshot newCapsule = capsuleEventMapper.toCapsuleSnapshot(event);
            CapsuleSnapshot savedCapsule = capsuleSnapshotRepository.save(newCapsule);

            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(savedCapsule, event.getSkillAtom());
            }
            if (event.getClusters() != null && !event.getClusters().isEmpty()) {
                smartUpdateCapsuleClusterMappings(savedCapsule, event.getClusters());
            }

            log.info("Successfully created skill capsule with ID: {}", savedCapsule.getCapsuleId());


            return savedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating capsule with ID: {}", event.getId(), e);
            throw new DuplicateCapsuleException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating capsule with ID: {}", event.getId(), e);
            throw new CapsuleProcessingException("Failed to create skill capsule", e);
        }
    }

    @Override
    public CapsuleSnapshot updateCapsule(CapsuleSnapshot existingCapsule, SkillCapsuleEvent event) {
        log.debug("Updating existing capsule {} with event data", existingCapsule.getCapsuleId());

        try {

            capsuleEventMapper.updateCapsuleSnapshot(event, existingCapsule);
            CapsuleSnapshot updatedCapsule = capsuleSnapshotRepository.save(existingCapsule);

            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(updatedCapsule, event.getSkillAtom());
            }
            if (event.getClusters() != null && !event.getClusters().isEmpty()) {
                smartUpdateCapsuleClusterMappings(updatedCapsule, event.getClusters());
            }

            log.info("Successfully updated skill capsule with ID: {}", updatedCapsule.getCapsuleId());
            return updatedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating capsule with ID: {}", existingCapsule.getCapsuleId(), e);
            throw new CapsuleProcessingException("Capsule update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating capsule with ID: {}", existingCapsule.getCapsuleId(), e);
            throw new CapsuleProcessingException("Failed to update skill capsule", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CapsuleSnapshot> findByCapsuleId(UUID capsuleId) {
        log.debug("Finding capsule by CapsuleId: {}", capsuleId);
        return capsuleSnapshotRepository.findByCapsuleId(capsuleId);
    }

    @Override
    public void smartUpdateCapsuleAtomMappings(CapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings) {
        atomMappingUpdater.smartUpdateMappings(capsule, skillAtomMappings);
    }

    @Override
    public void smartUpdateCapsuleClusterMappings(CapsuleSnapshot capsule, List<UUID> clusterIds) {
        clusterMappingUpdater.smartUpdateMappings(capsule, clusterIds);
    }

    @Override
    public CapsuleSnapshot assignAtomsToCapsule(SkillCapsuleEvent event) {
        log.info("Assigning atoms to capsule for capsuleId: {}", event.getId());

        try {
            // Find existing capsule with atom mappings - prevents lazy initialization error
            Optional<CapsuleSnapshot> existingCapsule = capsuleSnapshotRepository.findByCapsuleIdWithAtomMappings(event.getId());

            if (existingCapsule.isEmpty()) {
                throw new CapsuleNotFoundException(event.getId());
            }

            CapsuleSnapshot capsule = existingCapsule.get();
            log.info("Found existing capsule for assignment: {} with {} existing atom mappings",
                    capsule.getCapsuleId(), capsule.getCapsuleAtomMappings().size());

            // Use existing smart update logic to assign atoms
            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(capsule, event.getSkillAtom());

                // Save the updated capsule
                CapsuleSnapshot updatedCapsule = capsuleSnapshotRepository.save(capsule);

                log.info("Successfully assigned {} atoms to capsule {}, total mappings now: {}",
                        event.getSkillAtom().size(), updatedCapsule.getCapsuleId(),
                        updatedCapsule.getCapsuleAtomMappings().size());
                return updatedCapsule;
            } else {
                throw new CapsuleAtomMappingException("No skill atom mappings provided for assignment");
            }

        } catch (CapsuleNotFoundException e) {
            log.error("Capsule not found for assignment with ID: {}", event.getId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assigning atoms to capsule with ID: {}", event.getId(), e);
            throw new CapsuleProcessingException("Failed to assign atoms to capsule", e);
        }
    }

}
