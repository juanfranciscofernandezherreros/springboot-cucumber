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
    }
}

