package com.fernandez.backend.dto;

import lombok.Builder;

@Builder
public record InvitationAuditSnapshotDto(
        Long id,
        String email,
        String status
) {}

