package com.fernandez.backend.utils.constants;

public final class AdminApiPaths {

    private AdminApiPaths() {
        // Evita instanciación
    }

    public static final String BASE = "/api/v1/admin";
    // -------- CREACIÓN --------
    public static final String CREATE_USER = "/create-user";
    // -------- LISTADOS --------
    public static final String USERS = "/users";
    public static final String LOCKED_USERS = "/locked-users";
    // -------- CONSULTAS --------
    public static final String USER_STATUS = "/user-status";
    // -------- ACCIONES --------
    public static final String UNLOCK_USER = "/unlock";
    public static final String UPDATE_ROLE = "/update-role";
    public static final String DELETE_USER = "/delete";
    public static final String LOCK_USER = "/lock-user";
    public static final String UPDATE_USER = "/update-user";
    public static final String STATS = "/stats";
}
