package com.fernandez.backend.shared.constants;

public final class InvitationsApiPaths {

    // =========================
    // BASE PATHS
    // =========================
    public static final String API_BASE = "/api/v1";
    public static final String INVITATIONS_BASE = API_BASE + "/invitations";

    // =========================
    // RABBITMQ
    // =========================
    public static final String INVITATION_EXCHANGE = "invitation.exchange";
    public static final String INVITATION_CREATED_ROUTING_KEY = "invitation.created";

    // =========================
    // INVITATION CONFIG
    // =========================
    public static final int INVITATION_EXPIRATION_HOURS = 48;

    private InvitationsApiPaths() {
        // evita instanciación
    }
}

