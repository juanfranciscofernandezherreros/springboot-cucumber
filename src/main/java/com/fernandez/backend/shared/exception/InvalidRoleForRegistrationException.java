package com.fernandez.backend.shared.exception;

public class InvalidRoleForRegistrationException extends RuntimeException {

    public InvalidRoleForRegistrationException() {
        super("No está permitido registrar usuarios con este rol.");
    }

    public InvalidRoleForRegistrationException(String message) {
        super(message);
    }
}


