package com.fernandez.backend.shared.dto;

public record UserStatsResponseDto(
        long totalUsers,
        long blockedUsers,
        long pendingInvitations
) {}
