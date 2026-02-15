package com.fernandez.backend.shared.constants;

public class AuthServiceConstants {

    // --- Roles ---
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // --- Mensajes de Error ---
    public static final String ERR_ROLE_NOT_FOUND = "Rol %s no existe";
    public static final String ERR_FORBIDDEN_ADMIN_REG = "No está permitido registrar usuarios ADMIN";
    public static final String ERR_INVALID_REG_ROLE = "Rol no válido para el registro";
    public static final String ERR_EMAIL_EXISTS = "El email ya existe";
    public static final String ERR_USER_ALREADY_EXISTS = "El usuario con email %s ya existe";
    public static final String ERR_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERR_BAD_CREDENTIALS = "Credenciales inválidas";
    public static final String ERR_ACCOUNT_LOCKED_PERM = "Cuenta bloqueada permanentemente. Contacte con soporte.";
    public static final String ERR_ACCOUNT_LOCKED_TEMP = "Cuenta bloqueada. Inténtelo en %d segundos.";

    // --- Notificaciones: Email ---
    public static final String EMAIL_SUBJECT_WELCOME = "Bienvenido a la plataforma";
    public static final String EMAIL_BODY_WELCOME = "Hola %s,\n\nTu registro se ha completado correctamente.\n\n¡Bienvenido!";

    // --- Notificaciones: Telegram ---
    public static final String TELEGRAM_MSG_NEW_USER = "<b>Nuevo usuario registrado</b>\n"
            + "👤 Nombre: %s\n"
            + "📧 Email: %s\n"
            + "🌍 IP: %s";

    // --- Logs ---
    public static final String LOG_ADMIN_CREATED_USER = "ADMIN creó usuario {} con rol {}";
    public static final String LOG_USER_LOCKED = "Usuario {} bloqueado (nivel {})";
    public static final String LOG_LOGOUT = "Logout realizado";
    public static final String LOG_PASSWORD_UPDATED = "🔐 Contraseña actualizada desde perfil para {}";

    // --- Varios ---
    public static final String TOKEN_PREFIX = "Bearer ";

    private AuthServiceConstants() {
        // Constructor privado para evitar instanciación
    }
}