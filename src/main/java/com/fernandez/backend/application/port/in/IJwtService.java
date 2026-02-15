package com.fernandez.backend.application.port.in;

import com.fernandez.backend.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String generateToken(User user);
    String generateRefreshToken(User user);
    String extractUsername(String token);
    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver);
    boolean isTokenValid(String token, UserDetails userDetails);
}

