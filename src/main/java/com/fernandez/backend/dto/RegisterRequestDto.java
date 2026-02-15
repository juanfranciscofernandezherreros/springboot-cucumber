package com.fernandez.backend.dto;

public record RegisterRequestDto(
        String name,
        String email,
        String password,
        String role
) {
}

