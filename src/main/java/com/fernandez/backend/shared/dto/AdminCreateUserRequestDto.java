package com.fernandez.backend.shared.dto;

public record AdminCreateUserRequestDto(String name, String email, String password, String role) {}

