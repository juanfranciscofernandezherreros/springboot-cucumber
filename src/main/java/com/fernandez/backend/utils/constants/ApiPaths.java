package com.fernandez.backend.utils.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String API_V1 = "/api/v1";

    public static final class Auth {
        private Auth() {}
        public static final String BASE = "/auth";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/login";
        public static final String REFRESH_TOKEN = "/refresh-token";
        public static final String LOGOUT = "/logout";
        public static final String RESET_PASSWORD = "/reset-password";
    }

    public static final class Users {
        private Users() {}
        public static final String BASE = API_V1 + "/users";
        public static final String ME = "/me";
        public static final String UPDATE = "/update";
        public static final String CHANGE_PASSWORD = "/me/password";
    }

    public static final class Admin {
        private Admin() {}
        public static final String BASE = API_V1 + "/admin";
        public static final String CREATE_USER = "/create-user";
        public static final String USERS = "/users";
        public static final String LOCKED_USERS = "/locked-users";
        public static final String USER_STATUS = "/user-status";
        public static final String STATS = "/stats";
        public static final String LOCK_USER = "/lock-user";
        public static final String UNLOCK_USER = "/unlock";
        public static final String UPDATE_ROLE = "/update-role";
        public static final String UPDATE_USER = "/update-user";
        public static final String DELETE_USER = "/delete";
    }

    public static final class Invitations {
        private Invitations() {}
        public static final String BASE = API_V1 + "/admin/invitations";
        public static final String ALL = "/all";
        public static final String STATUSES = "/statuses";
        public static final String PENDING = "/pending";
        public static final String HISTORY = "/history";
        public static final String UPDATE_STATUS = "/{id}/status";
        public static final String DELETE = "/{id}";
        public static final String UPDATE = "/{id}";
    }

    public static final class Audit {
        private Audit() {}
        public static final String BASE = API_V1 + "/admin/audit";
        public static final String HISTORY = "/history";
        public static final String PATTERN = BASE + "/**";
    }

    // =========================
    // PATRONES DE SEGURIDAD
    // =========================
    public static final class Security {
        // Public routes
        public static final String PUBLIC_ROOT = "/";
        public static final String PUBLIC_INDEX = "/index.html";
        public static final String PUBLIC_STATIC = "/static/**";
        public static final String PUBLIC_AUTH = ApiPaths.Auth.BASE + "/**";
        public static final String PUBLIC_SWAGGER_UI = "/swagger-ui/**";
        public static final String PUBLIC_OPENAPI_DOCS = "/v3/api-docs/**";
        public static final String PUBLIC_H2_CONSOLE = "/h2-console/**";

        // Invitation patterns
        public static final String INVITATIONS_PATTERN = ApiPaths.Invitations.BASE + "/**";

        // Admin GET patterns
        public static final String ADMIN_USERS_GET = ApiPaths.Admin.BASE + ApiPaths.Admin.USERS;
        public static final String ADMIN_USER_STATUS_GET = ApiPaths.Admin.BASE + ApiPaths.Admin.USER_STATUS;
        public static final String ADMIN_STATS_GET = ApiPaths.Admin.BASE + ApiPaths.Admin.STATS;
        public static final String ADMIN_LOCKED_USERS_GET = ApiPaths.Admin.BASE + ApiPaths.Admin.LOCKED_USERS;

        // Admin POST patterns
        public static final String ADMIN_LOCK_USER_PATTERN = ApiPaths.Admin.BASE + ApiPaths.Admin.LOCK_USER + "/**";
        public static final String ADMIN_UNLOCK_USER_PATTERN = ApiPaths.Admin.BASE + ApiPaths.Admin.UNLOCK_USER + "/**";
        public static final String ADMIN_CREATE_USER = ApiPaths.Admin.BASE + ApiPaths.Admin.CREATE_USER;

        // Admin PUT patterns
        public static final String ADMIN_UPDATE_USER_PATTERN = ApiPaths.Admin.BASE + ApiPaths.Admin.UPDATE_USER + "/**";
        public static final String ADMIN_UPDATE_ROLE = ApiPaths.Admin.BASE + ApiPaths.Admin.UPDATE_ROLE;

        // Admin DELETE patterns
        public static final String ADMIN_DELETE_USER_PATTERN = ApiPaths.Admin.BASE + ApiPaths.Admin.DELETE_USER + "/**";

        // Audit patterns
        public static final String AUDIT_PATTERN = ApiPaths.Audit.BASE + "/**";

        // User patterns
        public static final String USERS_ME_PATTERN = ApiPaths.Users.BASE + ApiPaths.Users.ME + "/**";
        public static final String USERS_UPDATE = ApiPaths.Users.BASE + ApiPaths.Users.UPDATE;
        public static final String USERS_CHANGE_PASSWORD = ApiPaths.Users.BASE + ApiPaths.Users.CHANGE_PASSWORD;

        // Admin fallback
        public static final String ADMIN_PATTERN = ApiPaths.Admin.BASE + "/**";

        // Logout
        public static final String LOGOUT = ApiPaths.Auth.BASE + ApiPaths.Auth.LOGOUT;

        private Security() {}
    }
}
