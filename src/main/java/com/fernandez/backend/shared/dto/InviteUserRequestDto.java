package com.fernandez.backend.shared.dto;

import com.fernandez.backend.domain.model.Role;

public record InviteUserRequestDto(
        String name,
        String email,
        Role role
) {}
