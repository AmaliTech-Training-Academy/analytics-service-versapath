package com.capstone.exception;

public class CapsuleCompletionProcessingException extends RuntimeException {
    public CapsuleCompletionProcessingException(String message) {
        super(message);
    }
    public CapsuleCompletionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
