package com.capstone.service;

import com.capstone.model.UserSnapshot;
import org.common.event.ProduceUserEvent;

import java.util.Optional;
import java.util.UUID;

public interface UserSnapshotService {
    UserSnapshot processUserEvent(ProduceUserEvent event);

    UserSnapshot createUser(ProduceUserEvent event);

    UserSnapshot updateUser(UserSnapshot existingUser, ProduceUserEvent event);

    Optional<UserSnapshot> findByUserId(UUID userId);
}
