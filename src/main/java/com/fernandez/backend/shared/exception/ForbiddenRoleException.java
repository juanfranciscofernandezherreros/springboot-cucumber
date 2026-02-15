package com.fernandez.backend.shared.exception;

public class ForbiddenRoleException extends RuntimeException {
    public ForbiddenRoleException(String message) {
        super(message);
    }
}

