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
    public AtomSnapshot processSkillAtomEvent(SkillAtomEvent event) {
        log.info("Processing skill atom event for skillAtomId: {}", event.getId());

        try {
            Optional<AtomSnapshot> existingSkillAtom = atomSnapshotRepository.findByAtomId(event.getId());

            if (existingSkillAtom.isPresent()) {
                log.info("Skill atom exists, updating skill atom with ID: {}", event.getId());
                return updateSkillAtom(existingSkillAtom.get(), event);
            } else {
                log.info("Skill atom does not exist, creating new skill atom with ID: {}", event.getId());
                return createSkillAtom(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill atom event for skillAtomId: {}", event.getId(), e);
            throw new AtomProcessingException("Failed to process skill atom event", e);
        }
    }

    @Override
    public AtomSnapshot createSkillAtom(SkillAtomEvent event) {
        log.debug("Creating new skill atom from event: {}", event);

        try {
            AtomSnapshot newSkillAtom = atomEventMapper.toAtomSnapshot(event);
            AtomSnapshot savedSkillAtom = atomSnapshotRepository.save(newSkillAtom);
            log.info("Successfully created skill atom with ID: {}", savedSkillAtom.getAtomId());
            return savedSkillAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating skill atom with ID: {}", event.getId(), e);
            throw new AtomProcessingException("Skill atom creation failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error creating skill atom with ID: {}", event.getId(), e);
            throw new AtomProcessingException("Failed to create skill atom", e);
        }
    }

    @Override
    public AtomSnapshot updateSkillAtom(AtomSnapshot existingSkillAtom, SkillAtomEvent event) {
        log.debug("Updating existing skill atom {} with event data: {}", existingSkillAtom.getAtomId(), event);

        try {
            atomEventMapper.updateAtomSnapshot(event, existingSkillAtom);
            AtomSnapshot updatedSkillAtom = atomSnapshotRepository.save(existingSkillAtom);
            log.info("Successfully updated skill atom with ID: {}", updatedSkillAtom.getAtomId());
            return updatedSkillAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating skill atom with ID: {}", existingSkillAtom.getAtomId(), e);
            throw new AtomProcessingException("Skill atom update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating skill atom with ID: {}", existingSkillAtom.getAtomId(), e);
            throw new AtomProcessingException("Failed to update skill atom", e);
        }
    }

    @Override
    public Optional<AtomSnapshot> findBySkillAtomId(UUID skillAtomId) {
        log.debug("Finding skill atom by skillAtomId: {}", skillAtomId);
        return atomSnapshotRepository.findByAtomId(skillAtomId);
    }
}
