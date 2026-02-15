package com.fernandez.backend.dto;

public record LoginWithTotpRequest(
        String email,
        String password,
        String totpCode
) {
}
