package com.leave.management.system.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class InvalidSubjectException extends JWTVerificationException {
    public InvalidSubjectException(String message) {
        super(message);
    }
}
