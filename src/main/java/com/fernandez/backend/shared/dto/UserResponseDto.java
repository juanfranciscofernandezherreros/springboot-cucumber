package com.fernandez.backend.shared.dto;

import java.util.List;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        List<String> roles
) {}

