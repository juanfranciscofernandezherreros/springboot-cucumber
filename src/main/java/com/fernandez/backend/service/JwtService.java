package com.fernandez.backend.service;

import com.fernandez.backend.model.Privilege;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import com.fernandez.backend.utils.constants.ServiceStrings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String generateToken(User user) {
        return buildToken(user, jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpiration);
    }

    private String buildToken(User user, long expiration) {

        // ============================================================
        // CLAIMS DINÁMICOS: ROLES Y PRIVILEGIOS (AUTHORITIES)
        // ============================================================
        Map<String, Object> claims = new java.util.HashMap<>();

        claims.put(ServiceStrings.Jwt.CLAIM_NAME, user.getName() != null ? user.getName() : ServiceStrings.Common.EMPTY);

        if (user.getRoles() != null) {
            // 1. Extraemos los nombres de los Roles (ej. ADMIN, PREMIUM)
            java.util.List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            claims.put(ServiceStrings.Jwt.CLAIM_ROLES, roleNames);

            // 2. EXTRAEMOS LOS PRIVILEGIOS (La magia del Many-to-Many)
            // Recorremos cada rol, entramos en su Set de privilegios y los unificamos
            java.util.List<String> authorities = user.getRoles().stream()
                    .flatMap(role -> role.getPrivileges().stream())
                    .map(Privilege::getName)
                    .distinct() // Evitamos duplicados si dos roles comparten un privilegio
                    .collect(Collectors.toList());

            // Estas "authorities" son las que hasAuthority('...') buscará en el backend
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

    // =====================
    // EXTRACCIÓN
    // =====================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

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

    // =====================
    // VALIDACIÓN
    // =====================
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}