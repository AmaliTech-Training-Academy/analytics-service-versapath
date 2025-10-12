package com.capstone.exception;

public class ClusterProcessingException extends RuntimeException {
    public ClusterProcessingException(String message) {
        super(message);
    }
    public ClusterProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
