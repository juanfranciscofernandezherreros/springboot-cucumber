package com.fernandez.backend.dto;

import java.time.Instant;

/**
 * DTO que representa el evento de invitación creada.
 * Se utiliza para transportar la información desde el Controller
 * hacia RabbitMQ y finalmente al Dashboard/Consumer.
 */
public record InvitationCreatedEvent(
        String email,
        String token,
        Instant expiresAt
) {}