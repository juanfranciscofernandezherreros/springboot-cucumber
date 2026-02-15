package com.fernandez.backend.dto;

public record AdminCreateUserRequestDto(String name, String email, String password, String role) {}

