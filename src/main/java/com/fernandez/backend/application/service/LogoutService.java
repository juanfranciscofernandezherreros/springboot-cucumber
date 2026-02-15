package com.fernandez.backend.application.service;

import com.fernandez.backend.application.port.in.ILogoutService;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.TokenRepository;
import com.fernandez.backend.shared.constants.ServiceStrings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
public class LogoutService implements ILogoutService {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(ServiceStrings.Common.AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(ServiceStrings.Common.BEARER_PREFIX)) {
            return;
        }
        final String jwt = authHeader.substring(ServiceStrings.Common.BEARER_PREFIX.length());
        var storedToken = tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}
