package com.capstone.service;

import com.capstone.model.AtomSnapshot;
import org.common.event.SkillAtomEvent;

import java.util.Optional;
import java.util.UUID;

public interface AtomSnapshotService {

    AtomSnapshot processSkillAtomEvent(SkillAtomEvent event);
    AtomSnapshot createSkillAtom(SkillAtomEvent event);
    AtomSnapshot updateSkillAtom(AtomSnapshot existingSkillAtom, SkillAtomEvent event);
    Optional<AtomSnapshot> findBySkillAtomId(UUID skillAtomId);
}
