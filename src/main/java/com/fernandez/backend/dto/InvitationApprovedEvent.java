package com.fernandez.backend.dto;

public record InvitationApprovedEvent(
        Long invitationId,
        String email,
        String token
) {}
