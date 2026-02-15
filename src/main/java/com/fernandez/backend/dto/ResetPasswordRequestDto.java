package com.fernandez.backend.dto;

/**
 * DTO para la solicitud de restablecimiento de contraseña.
 */
public record ResetPasswordRequestDto(
        String email,
        String newPassword
) {}

