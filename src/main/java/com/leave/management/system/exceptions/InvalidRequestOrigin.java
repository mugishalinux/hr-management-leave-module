package com.leave.management.system.exceptions;

public class InvalidRequestOrigin extends RuntimeException {
    public InvalidRequestOrigin(String message) {
        super(message);
    }
}
