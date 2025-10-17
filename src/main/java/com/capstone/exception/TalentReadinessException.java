package com.capstone.exception;

public class TalentReadinessException extends RuntimeException {
    public TalentReadinessException(String message) {
        super(message);
    }
    public TalentReadinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
