package com.capstone.service;

import com.capstone.model.CapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CapsuleSnapshotService {
    CapsuleSnapshot processCapsuleEvent(SkillCapsuleEvent event);
    CapsuleSnapshot createCapsule(SkillCapsuleEvent event);
    CapsuleSnapshot updateCapsule(CapsuleSnapshot existingCapsule, SkillCapsuleEvent event);
    Optional<CapsuleSnapshot> findByCapsuleId(UUID skillCapsuleId);
    void smartUpdateCapsuleAtomMappings(CapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings);
    void smartUpdateCapsuleClusterMappings(CapsuleSnapshot capsule, List<UUID> clusterIds);
}
