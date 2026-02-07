package com.fernandez.backend.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserAuditSnapshotDto(
        Long id,
        String name,
        String email,
        List<String> roles,
        boolean accountNonLocked
) {}

