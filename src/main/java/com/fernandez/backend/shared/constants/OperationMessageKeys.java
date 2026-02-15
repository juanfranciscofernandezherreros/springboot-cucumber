package com.fernandez.backend.shared.constants;

/**
 * Claves para mensajes operacionales (controllers/services).
 * Se resuelven vía DB mediante OperationMessages.
 */
public final class OperationMessageKeys {
    private OperationMessageKeys() {}

    public static final String ADMIN_USER_CREATED = "admin.user.created";
    public static final String ADMIN_USER_LOCKED = "admin.user.locked";
    public static final String ADMIN_USER_UNLOCKED = "admin.user.unlocked";
    public static final String ADMIN_ROLE_UPDATED = "admin.user.role.updated";
    public static final String ADMIN_USER_UPDATED = "admin.user.updated";
    public static final String ADMIN_USER_DELETED = "admin.user.deleted";

    public static final String USER_PASSWORD_CHANGED = "user.password.changed";
}

