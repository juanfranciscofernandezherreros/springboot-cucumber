package com.fernandez.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateInvitationRequestDto(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String description
) {}

