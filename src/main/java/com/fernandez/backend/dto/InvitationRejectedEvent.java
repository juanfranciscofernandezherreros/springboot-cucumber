package com.fernandez.backend.dto;

public record InvitationRejectedEvent(
        Long invitationId,
        String email,
        String reason
) {}

