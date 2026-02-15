package com.fernandez.backend.service;

import com.fernandez.backend.repository.TokenRepository;
import com.fernandez.backend.utils.constants.ServiceStrings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader(ServiceStrings.Common.AUTH_HEADER);
        final String jwt;
        if (authHeader == null || !authHeader.startsWith(ServiceStrings.Common.BEARER_PREFIX)) {
            return;
        }
        jwt = authHeader.substring(ServiceStrings.Common.BEARER_PREFIX.length());
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}