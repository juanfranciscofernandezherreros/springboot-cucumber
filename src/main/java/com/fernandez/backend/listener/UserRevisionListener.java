package com.fernandez.backend.listener;

import com.fernandez.backend.model.CustomRevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Listener de Auditoría Nivel Maestro.
 * Captura automáticamente el usuario y la IP en cada transacción de base de datos.
 */
public class UserRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity rev = (CustomRevisionEntity) revisionEntity;

        // 1. Capturar el Usuario desde el JWT (SecurityContext)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // Guardamos el email del Admin o Usuario que realiza la acción
            rev.setModifierUser(auth.getName());
        } else {
            rev.setModifierUser("SYSTEM_GENERATED"); // Por ejemplo, durante el registro inicial
        }

        // 2. Capturar la IP del Request actual
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Manejo de Proxies (Nginx/Cloudflare) para obtener la IP real
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            String clientIp = (xForwardedFor != null && !xForwardedFor.isEmpty())
                    ? xForwardedFor.split(",")[0]
                    : request.getRemoteAddr();

            rev.setIpAddress(clientIp);
        }
    }
}