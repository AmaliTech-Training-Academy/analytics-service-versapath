package com.capstone.exception;

import java.util.UUID;

public class CapsuleNotFoundException extends RuntimeException {
    public CapsuleNotFoundException(String message) {
        super(message);
    }
    public CapsuleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public CapsuleNotFoundException(UUID capsuleId) {
        super("Skill capsule not found with ID: " + capsuleId);
    }
}
