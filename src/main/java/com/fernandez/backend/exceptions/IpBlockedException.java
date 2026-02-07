package com.fernandez.backend.exceptions;

public class IpBlockedException extends RuntimeException {

    public IpBlockedException() {
        super("Tu IP ha sido bloqueada temporalmente por motivos de seguridad.");
    }

    public IpBlockedException(String message) {
        super(message);
    }
}
