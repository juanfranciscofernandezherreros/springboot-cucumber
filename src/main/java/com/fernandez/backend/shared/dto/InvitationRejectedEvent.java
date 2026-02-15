package com.fernandez.backend.shared.dto;

public record InvitationRejectedEvent(
        Long invitationId,
        String email,
        String reason
) {}

