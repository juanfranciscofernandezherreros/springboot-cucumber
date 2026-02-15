package com.fernandez.backend.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminUserListResponseDto(
        Long id,
        String name,
        String email,
        List<String> roles,
        boolean accountNonLocked,
        int failedAttempt,
        int lockCount
) {}

