package com.fernandez.backend.dto;

public record AdminUpdateUserRequest(
        String name,
        String role,          // ⚠️ NO List
        Boolean accountNonLocked
) {}
