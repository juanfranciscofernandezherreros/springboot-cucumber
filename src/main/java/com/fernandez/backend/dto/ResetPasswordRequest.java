package com.fernandez.backend.dto;

/**
 * DTO para la solicitud de restablecimiento de contraseña.
 * @param email Correo del usuario que desea cambiar la clave.
 * @param newPassword La nueva contraseña que se cifrará en el servidor.
 */
public record ResetPasswordRequest(
        String email,
        String newPassword
) {}