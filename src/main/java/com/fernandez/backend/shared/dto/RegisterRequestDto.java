package com.fernandez.backend.shared.dto;

public record RegisterRequestDto(
        String name,
        String email,
        String password,
        String role
) {
}
