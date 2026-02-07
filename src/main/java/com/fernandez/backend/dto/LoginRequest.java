package com.fernandez.backend.dto;

public record LoginRequest(
        String email,
        String password
) {
}