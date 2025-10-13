package com.capstone.exception;

public class AssessmentProcessingException extends RuntimeException {
    public AssessmentProcessingException(String message) {
        super(message);
    }

    public AssessmentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
