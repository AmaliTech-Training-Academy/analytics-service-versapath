package com.capstone.exception;

import java.util.UUID;

public class ClusterNotFoundException extends RuntimeException {
    public ClusterNotFoundException(String message) {
        super(message);
    }
    public ClusterNotFoundException(UUID skillAtomId) {
        super("Skill atom not found with ID: " + skillAtomId);
    }
    public ClusterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
