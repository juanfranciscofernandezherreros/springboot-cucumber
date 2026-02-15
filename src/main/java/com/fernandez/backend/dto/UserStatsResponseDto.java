package com.fernandez.backend.dto;

public record UserStatsResponseDto(
        long totalUsers,
        long blockedUsers,
        long pendingInvitations
) {}

