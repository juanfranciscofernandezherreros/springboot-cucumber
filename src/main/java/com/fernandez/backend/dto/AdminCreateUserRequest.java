package com.fernandez.backend.dto;


public record AdminCreateUserRequest(
        String name,
        String email,
        String password,
        String role
) {}

