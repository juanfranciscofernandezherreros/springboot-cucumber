package com.fernandez.backend.utils.constants;


public final class AuthEndpoints {

    private AuthEndpoints() {
        // Evita instanciación
    }

    public static final String BASE = "/auth";

    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String REFRESH_TOKEN = "/refresh-token";
    public static final String LOGOUT = "/logout";
    public static final String RESET_PASSWORD = "/reset-password";
}

