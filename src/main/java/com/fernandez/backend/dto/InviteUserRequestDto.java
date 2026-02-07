package com.fernandez.backend.dto;

import com.fernandez.backend.model.Role;

public record InviteUserRequestDto(
        String name,
        String email,
        Role role
) {}

