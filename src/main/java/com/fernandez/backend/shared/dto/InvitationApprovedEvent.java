package com.fernandez.backend.shared.dto;

public record InvitationApprovedEvent(
        Long invitationId,
        String email,
        String token
) {}
