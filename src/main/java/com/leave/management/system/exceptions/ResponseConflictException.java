package com.leave.management.system.exceptions;

public class ResponseConflictException extends RuntimeException {
    public ResponseConflictException(String message) {
        super(message);
    }
}
