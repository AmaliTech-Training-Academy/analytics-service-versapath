package com.capstone.exception;

public class AtomProcessingException extends RuntimeException {
    public AtomProcessingException(String message) {
        super(message);
    }
    public AtomProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
