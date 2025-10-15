package com.capstone.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}
