package com.capstone.exception;

public class CapsuleProcessingException extends RuntimeException {
    public CapsuleProcessingException(String message) {
        super(message);
    }
    public CapsuleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
