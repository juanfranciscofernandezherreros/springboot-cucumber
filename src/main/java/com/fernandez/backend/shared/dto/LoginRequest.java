package com.fernandez.backend.shared.dto;

public record LoginRequest(
        String email,
        String password
) {
}