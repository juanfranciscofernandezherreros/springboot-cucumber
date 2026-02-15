package com.fernandez.backend.application.service;

import com.fernandez.backend.application.port.in.IJwtService;
import com.fernandez.backend.domain.model.Privilege;
import com.fernandez.backend.domain.model.Role;
import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.shared.constants.ServiceStrings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

// Sin @Service: este bean se crea explícitamente en ServiceBeansConfig
@RequiredArgsConstructor
public class JwtService implements IJwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    public String generateToken(User user) {
        return buildToken(user, jwtExpiration);
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpiration);
    }

    private String buildToken(User user, long expiration) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(ServiceStrings.Jwt.CLAIM_NAME, user.getName() != null ? user.getName() : ServiceStrings.Common.EMPTY);

        if (user.getRoles() != null) {
            var roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            claims.put(ServiceStrings.Jwt.CLAIM_ROLES, roleNames);

            var authorities = user.getRoles().stream()
                    .flatMap(role -> role.getPrivileges().stream())
                    .map(Privilege::getName)
                    .distinct()
                    .collect(Collectors.toList());

            claims.put(ServiceStrings.Jwt.CLAIM_AUTHORITIES, authorities);
        } else {
            claims.put(ServiceStrings.Jwt.CLAIM_ROLES, java.util.List.of());
            claims.put(ServiceStrings.Jwt.CLAIM_AUTHORITIES, java.util.List.of());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setId(UUID.randomUUID().toString())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
