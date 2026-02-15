package com.fernandez.backend.shared.constants;

/**
 * Constantes de cadenas (strings) utilizadas por los servicios.
 * Agrupadas por servicio para facilitar su localización y traducción.
 */
public final class ServiceStrings {

    private ServiceStrings() {}

    public static final class Common {
        private Common() {}
        public static final String AUTH_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String EMPTY = "";
        public static final String ROLE_ADMIN = "ADMIN";
    }

    public static final class Auth {
        private Auth() {}
        public static final String TOKEN_REFRESHED = "🔄 Token renovado con éxito para el usuario: {}";
        public static final String REFRESH_FAILED = "⚠️ Intento de refresh token fallido o token inválido";
    }

    public static final class Jwt {
        private Jwt() {}
        public static final String CLAIM_NAME = "name";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_AUTHORITIES = "authorities";
    }

    public static final class IpLock {
        private IpLock() {}
        public static final String IP_PREFIX = "IP_ATTEMPT:";
        public static final String IP_BLOCKED_PREFIX = "IP_BLOCKED:";
        public static final String LOG_ENABLED = "\ud83d\udee1\ufe0f SEGURIDAD: Bloqueo IP ACTIVADO. M\u00e1x Intentos: {}, Tiempo Bloqueo: {} min";
        public static final String LOG_DISABLED = "\u26a0\ufe0f SEGURIDAD: El sistema de bloqueo por IP (Redis) est\u00e1 DESACTIVADO desde config.";
        public static final String LOG_REDIS_ERROR_ISIP = "Error al consultar Redis (isIpBlocked): {}";
        public static final String LOG_REDIS_ATTEMPT = "\ud83d\udd0d REDIS: Intento fallido registrado para IP: {}. Intentos actuales: {}/{}";
        public static final String LOG_REDIS_BLOCK = "\ud83d\udeab REDIS: \u00a1IP BLOQUEADA! -> {} por {} minutos";
        public static final String LOG_REDIS_CONNECTION_ERROR = "\u274c REDIS: Error al conectar con el servidor Redis: {}";
    }

    public static final class Logout {
        private Logout() {}
    }

    public static final class Email {
        private Email() {}
        public static final String LOG_WARN_CANT_SEND = "No se pudo enviar el email a {}";
    }

    public static final class Telegram {
        private Telegram() {}
        public static final String URL_FORMAT = "https://api.telegram.org/bot%s/sendMessage";
        public static final String FIELD_CHAT_ID = "chat_id";
        public static final String FIELD_TEXT = "text";
        public static final String FIELD_PARSE_MODE = "parse_mode";
        public static final String PARSE_MODE_HTML = "HTML";
    }

    public static final class Audit {
        private Audit() {}
        public static final String REV_PREFIX = "REV-";
        public static final String DEFAULT_AUTHOR = "admin";
        public static final String DESC_USER_PREFIX = "User: ";
        public static final String DESC_INV_PREFIX = "Inv: ";
        public static final String DESC_ID_PREFIX = "ID: ";
    }

    public static final class User {
        private User() {}
        public static final String LOG_METRICS = "M\u00c9TRICAS: Total={}, Bloqueados={}, InvitacionesPendientes={}";
        public static final String LOG_ADMIN_UNLOCKED = "ADMIN: Usuario {} desbloqueado";
        public static final String LOG_ADMIN_LOCKED = "ADMIN: Usuario {} bloqueado";
        public static final String ERR_CANNOT_DELETE_ADMIN = "No est\u00e1 permitido eliminar a otros administradores.";
        public static final String LOG_ADMIN_DELETED = "ADMIN: Usuario con id {} eliminado";
        public static final String ERR_INVALID_ROLE_PREFIX = "Rol no v\u00e1lido: ";
        public static final String ERR_CANNOT_DEGRADE_ADMIN = "No puedes degradar a otro ADMIN.";
        public static final String LOG_ROLE_UPDATED = "ADMIN: Rol de {} actualizado a {}";
        public static final String ERR_CANNOT_MODIFY_ADMIN = "No est\u00e1 permitido modificar a otros administradores.";
        public static final String ERR_USER_NOT_FOUND_PREFIX = "Usuario no encontrado: ";
        public static final String ERR_USER_NOT_FOUND_ID_PREFIX = "Usuario no encontrado con id: ";
        public static final String ROLE_ADMIN = "ADMIN";
    }
}

