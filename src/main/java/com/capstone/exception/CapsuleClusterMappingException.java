package com.capstone.exception;

import java.util.UUID;

public class CapsuleClusterMappingException extends RuntimeException {

    public CapsuleClusterMappingException(String message) {
        super(message);
    }

    public CapsuleClusterMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CapsuleClusterMappingException(UUID capsuleId, UUID clusterId, String reason) {
        super("Failed to map cluster " + clusterId + " to capsule " + capsuleId + ": " + reason);
    }
}
