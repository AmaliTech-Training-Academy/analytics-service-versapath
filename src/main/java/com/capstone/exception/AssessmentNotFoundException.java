package com.capstone.exception;

import java.util.UUID;

public class AssessmentNotFoundException extends RuntimeException {
    public AssessmentNotFoundException(String message) {
        super(message);
    }

    public AssessmentNotFoundException(UUID userId, UUID assessmentId, Integer attemptNumber) {
        super("Assessment not found for userId: " + userId +
                ", assessmentId: " + assessmentId +
                ", attempt: " + attemptNumber);
    }

    public AssessmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
