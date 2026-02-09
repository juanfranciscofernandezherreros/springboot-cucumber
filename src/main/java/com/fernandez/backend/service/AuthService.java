package com.fernandez.backend.service;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.exceptions.*;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import com.fernandez.backend.model.Token;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.TokenRepository;
import com.fernandez.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final IpLockService ipLockService;
    private final RoleRepository roleRepository;
    private final TelegramService telegramService;

    private long getLockDuration(int lockCount) {
        return switch (lockCount) {
            case 1 -> 1 * 60 * 1000;
            case 2 -> 2 * 60 * 1000;
            case 3 -> 3 * 60 * 1000;
            default -> -1L;
        };
    }

    /* ===================== REGISTRO ===================== */

    @Transactional
    public void register(RegisterRequest request, String clientIp) {

        if (ipLockService.isIpBlocked(clientIp)) {
            throw new IpBlockedException();
        }

        Role userRole = roleRepository.findByName(request.role())
                .orElseThrow(() -> new RuntimeException("Rol USER no existe"));

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public void registerPublic(RegisterRequest request, String clientIp) {

        if (ipLockService.isIpBlocked(clientIp)) {
            throw new IpBlockedException();
        }

        if (request.role() != null) {

            if ("ADMIN".equalsIgnoreCase(request.role())) {
                throw new ForbiddenRoleException(
                        "No está permitido registrar usuarios ADMIN"
                );
            }

            if (!"USER".equalsIgnoreCase(request.role())) {
                throw new InvalidRoleException(
                        "Rol no válido para el registro"
                );
            }
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email ya existe");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Rol USER no existe"));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);

        // 📣 Notificación a Telegram
        telegramService.sendMessage(
                "<b>Nuevo usuario registrado</b>\n"
                        + "👤 Nombre: " + request.name() + "\n"
                        + "📧 Email: " + request.email() + "\n"
                        + "🌍 IP: " + clientIp
        );
    }

    @Transactional
    public void registerByAdmin(AdminCreateUserRequest request) {

        Role role = roleRepository.findByName(request.role().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Rol no válido"));

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(role))
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        userRepository.save(user);

        log.info("ADMIN creó usuario {} con rol {}", request.email(), role.getName());
    }


    /* ===================== LOGIN ===================== */

    public TokenResponse login(LoginRequest request, String clientIp) {

        if (ipLockService.isIpBlocked(clientIp)) {
            throw new IpBlockedException();
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    ipLockService.registerFailedAttempt(clientIp);
                    return new BadCredentialsException("Credenciales inválidas");
                });

        if (!user.isAccountNonLocked()) {
            long duration = getLockDuration(user.getLockCount());
            if (duration == -1L) {
                throw new LockedException("Cuenta bloqueada permanentemente. Contacte con soporte.");
            }

            if (shouldUnlock(user)) {
                unlockUser(user);
            } else {
                long timeLeft = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
                throw new LockedException("Cuenta bloqueada. Inténtelo en " + (timeLeft / 1000) + " segundos.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            resetAllAttempts(user);

            var accessToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user);
            saveUserToken(user, accessToken);
            saveUserToken(user, refreshToken);

            return new TokenResponse(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            updateFailedAttempts(request.email());
            ipLockService.registerFailedAttempt(clientIp);
            throw e;
        }
    }

    /* ===================== MÉTODOS PRIVADOS ===================== */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(this::processFailedAttempt);
    }

    @Transactional
    public void unlockUser(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    private void processFailedAttempt(User user) {
        int attempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(new Date());
            user.setLockCount(user.getLockCount() + 1);
            user.setFailedAttempt(0);
            log.warn("Usuario {} bloqueado (nivel {})", user.getEmail(), user.getLockCount());
        }

        userRepository.saveAndFlush(user);
    }

    private boolean shouldUnlock(User user) {
        if (user.getLockTime() == null) return true;
        long duration = getLockDuration(user.getLockCount());
        if (duration == -1L) return false;
        return user.getLockTime().getTime() + duration < System.currentTimeMillis();
    }

    private void resetAllAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var tokens = tokenRepository.findAllValidTokenByUser(user.getId());
        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    /* ===================== LOGOUT / REFRESH ===================== */

    @Transactional
    public TokenResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        try {
            String refreshToken = authHeader.substring(7);
            String email = jwtService.extractUsername(refreshToken);

            var user = userRepository.findByEmail(email).orElseThrow();
            var storedToken = tokenRepository.findByToken(refreshToken).orElse(null);

            if (storedToken != null && !storedToken.isRevoked()
                    && jwtService.isTokenValid(refreshToken, user)) {

                revokeAllUserTokens(user);

                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                saveUserToken(user, accessToken);
                saveUserToken(user, newRefreshToken);

                return new TokenResponse(accessToken, newRefreshToken);
            }
        } catch (Exception e) {
            log.error("Error en refresh token", e);
        }
        return null;
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

        tokenRepository.findByToken(authHeader.substring(7)).ifPresent(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
            SecurityContextHolder.clearContext();
            log.info("Logout realizado");
        });
    }

    @Transactional
    public TokenResponse resetPasswordFromProfile(String email, String newPassword) {

        // 1️⃣ Buscar usuario autenticado
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        // 3️⃣ Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4️⃣ Invalidar TODOS los tokens activos
        revokeAllUserTokens(user);

        // 5️⃣ Resetear estado de seguridad
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);

        // 6️⃣ Generar NUEVOS tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 7️⃣ Guardarlos
        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        userRepository.save(user);

        log.info("🔐 Contraseña actualizada desde perfil para {}", email);

        // 8️⃣ Devolver tokens nuevos
        return new TokenResponse(accessToken, refreshToken);
    }


}
