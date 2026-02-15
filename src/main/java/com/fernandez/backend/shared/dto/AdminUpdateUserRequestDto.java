package com.fernandez.backend.shared.dto;

public record AdminUpdateUserRequestDto(
        String name,
        String role,
        Boolean accountNonLocked
) {}
