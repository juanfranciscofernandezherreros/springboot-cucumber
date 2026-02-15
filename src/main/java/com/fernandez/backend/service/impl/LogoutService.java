package com.fernandez.backend.service.impl;

import com.fernandez.backend.repository.TokenRepository;
import com.fernandez.backend.service.ILogoutService;
import com.fernandez.backend.utils.constants.ServiceStrings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
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

