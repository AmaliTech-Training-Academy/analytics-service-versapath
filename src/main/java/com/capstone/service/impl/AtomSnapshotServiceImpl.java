package com.capstone.service.impl;

import com.capstone.exception.AtomProcessingException;
import com.capstone.mapper.AtomEventMapper;
import com.capstone.model.AtomSnapshot;
import com.capstone.repository.AtomSnapshotRepository;
import com.capstone.service.AtomSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillAtomEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AtomSnapshotServiceImpl implements AtomSnapshotService {

    private final AtomSnapshotRepository atomSnapshotRepository;
    private final AtomEventMapper atomEventMapper;


    @Override
    public AtomSnapshot processAtomEvent(SkillAtomEvent event) {
        log.info("Processing skill atom event for skillAtomId: {}", event.getId());

        try {
            Optional<AtomSnapshot> existingSkillAtom = atomSnapshotRepository.findByAtomId(event.getId());

            if (existingSkillAtom.isPresent()) {
                log.info("Skill atom exists, updating skill atom with ID: {}", event.getId());
                return updateAtom(existingSkillAtom.get(), event);
            } else {
                log.info("Skill atom does not exist, creating new skill atom with ID: {}", event.getId());
                return createAtom(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill atom event for skillAtomId: {}", event.getId(), e);
            throw new AtomProcessingException("Failed to process skill atom event", e);
        }
    }

    @Override
    public AtomSnapshot createAtom(SkillAtomEvent event) {
        log.debug("Creating new skill atom from event: {}", event);

        try {
            AtomSnapshot newAtom = atomEventMapper.toAtomSnapshot(event);
            AtomSnapshot savedAtom = atomSnapshotRepository.save(newAtom);
            log.info("Successfully created skill atom with ID: {}", savedAtom.getAtomId());
            return savedAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating skill atom with ID: {}", event.getId(), e);
            throw new AtomProcessingException("Skill atom creation failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error creating skill atom with ID: {}", event.getId(), e);
            throw new AtomProcessingException("Failed to create skill atom", e);
        }
    }

    @Override
    public AtomSnapshot updateAtom(AtomSnapshot existingAtom, SkillAtomEvent event) {
        log.debug("Updating existing skill atom {} with event data: {}", existingAtom.getAtomId(), event);

        try {
            atomEventMapper.updateAtomSnapshot(event, existingAtom);
            AtomSnapshot updatedSkillAtom = atomSnapshotRepository.save(existingAtom);
            log.info("Successfully updated skill atom with ID: {}", updatedSkillAtom.getAtomId());
            return updatedSkillAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating skill atom with ID: {}", existingAtom.getAtomId(), e);
            throw new AtomProcessingException("Skill atom update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating skill atom with ID: {}", existingAtom.getAtomId(), e);
            throw new AtomProcessingException("Failed to update skill atom", e);
        }
    }

    @Override
    public Optional<AtomSnapshot> findByAtomId(UUID atomId) {
        log.debug("Finding skill atom by skillAtomId: {}", atomId);
        return atomSnapshotRepository.findByAtomId(atomId);
    }
}
