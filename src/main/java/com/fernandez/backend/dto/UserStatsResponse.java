package com.fernandez.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatsResponse {
    private long totalUsers;
    private long blockedUsers;
    private long pendingInvitations;
}
