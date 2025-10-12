package com.capstone.service;

import com.capstone.model.AtomSnapshot;
import org.common.event.SkillAtomEvent;

import java.util.Optional;
import java.util.UUID;

public interface AtomSnapshotService {

    AtomSnapshot processAtomEvent(SkillAtomEvent event);
    AtomSnapshot createAtom(SkillAtomEvent event);
    AtomSnapshot updateAtom(AtomSnapshot existingAtom, SkillAtomEvent event);
    Optional<AtomSnapshot> findByAtomId(UUID atomId);
}
