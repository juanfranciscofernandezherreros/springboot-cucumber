package com.fernandez.backend.model;

import java.util.List;

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    APPROVED;

    public boolean canTransitionTo(InvitationStatus nextStatus) {
        return switch (this) {
            case PENDING -> List.of(ACCEPTED, REJECTED, EXPIRED).contains(nextStatus);

            // Una vez aceptada, puede aprobarse definitivamente o rechazarse tras revisión
            case ACCEPTED -> List.of(APPROVED, REJECTED).contains(nextStatus);

            // Si está expirada, permitimos re-activarla o rechazarla definitivamente
            case EXPIRED -> List.of(PENDING, REJECTED).contains(nextStatus);

            // Cambio solicitado: REJECTED ahora puede volver a PENDING
            case REJECTED -> List.of(PENDING).contains(nextStatus);

            // APPROVED sigue siendo el único estado final (éxito total)
            case APPROVED -> false;

            default -> false;
        };
    }
}