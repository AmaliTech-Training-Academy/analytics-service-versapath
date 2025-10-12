package com.capstone.exception;

import java.util.UUID;

public class CapsuleAtomMappingException extends RuntimeException {

    public CapsuleAtomMappingException(String message) {
        super(message);
    }

    public CapsuleAtomMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CapsuleAtomMappingException(UUID capsuleId, UUID atomId, String reason) {
        super("Failed to map atom " + atomId + " to capsule " + capsuleId + ": " + reason);
    }
}
