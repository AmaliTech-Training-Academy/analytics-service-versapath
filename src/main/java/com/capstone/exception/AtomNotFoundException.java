package com.capstone.exception;

import java.util.UUID;

public class AtomNotFoundException extends RuntimeException {
    public AtomNotFoundException(String message) {
        super(message);
    }
    public AtomNotFoundException(UUID skillAtomId) {
        super("Skill atom not found with ID: " + skillAtomId);
    }
    public AtomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
