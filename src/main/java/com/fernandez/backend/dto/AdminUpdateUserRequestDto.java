package com.fernandez.backend.dto;

public record AdminUpdateUserRequestDto(
        String name,
        String role,
        Boolean accountNonLocked
) {}

