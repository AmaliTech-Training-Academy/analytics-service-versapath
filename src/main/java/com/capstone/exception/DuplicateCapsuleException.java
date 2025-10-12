package com.capstone.exception;

import java.util.UUID;

public class DuplicateCapsuleException extends RuntimeException {
    public DuplicateCapsuleException(String message) {
        super(message);
    }
    public DuplicateCapsuleException(UUID skillCapsuleId) {
        super("Skill capsule already exists with ID: " + skillCapsuleId);
    }
}
