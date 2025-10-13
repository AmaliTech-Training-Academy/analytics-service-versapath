package com.capstone.exception;

import java.util.UUID;

public class DuplicateAssessmentAttemptException extends RuntimeException {
    public DuplicateAssessmentAttemptException(String message) {
        super(message);
    }

    public DuplicateAssessmentAttemptException(UUID userId, UUID assessmentId, Integer attemptNumber) {
        super("Assessment attempt already exists for userId: " + userId +
                ", assessmentId: " + assessmentId +
                ", attempt: " + attemptNumber);
    }
}
