package com.fernandez.backend.service;

import com.fernandez.backend.config.SecurityLockProperties;
import com.fernandez.backend.config.SecurityNotificationProperties;
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

import static com.fernandez.backend.utils.constants.AuthServiceConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IpLockService ipLockService;
    private final RoleRepository roleRepository;
    private final TelegramService telegramService;
    private final EmailService emailService;
    private final SecurityNotificationProperties notificationProperties;
    private final SecurityLockProperties lockProperties;
    private final TwoFactorService twoFactorService;

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private long getLockDuration(int lockCount) {
        return lockProperties.getDurations().getOrDefault(lockCount, -1L);
    }

    @Transactional
    public void registerPublic(RegisterRequest request, String clientIp) {
        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();

        if (request.role() != null) {
            if (ROLE_ADMIN.equalsIgnoreCase(request.role())) throw new ForbiddenRoleException(ERR_FORBIDDEN_ADMIN_REG);
            if (!ROLE_USER.equalsIgnoreCase(request.role())) throw new InvalidRoleException(ERR_INVALID_REG_ROLE);
        }

        if (userRepository.existsByEmail(request.email())) throw new EmailAlreadyExistsException(ERR_EMAIL_EXISTS);

        Role userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(String.format(ERR_ROLE_NOT_FOUND, ROLE_USER)));

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

        if (notificationProperties.isSendDataEmailEnabled()) {
            emailService.sendEmail(user.getEmail(), EMAIL_SUBJECT_WELCOME, String.format(EMAIL_BODY_WELCOME, user.getName()));
        }

        if (notificationProperties.isSendDataTelegramEnabled()) {
            telegramService.sendMessage(String.format(TELEGRAM_MSG_NEW_USER, request.name(), request.email(), clientIp));
        }
    }

    @Transactional
    public AdminUserListResponse registerByAdmin(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(String.format(ERR_USER_ALREADY_EXISTS, request.email()));
        }

        Role role = roleRepository.findByName(request.role().toUpperCase())
                .orElseThrow(() -> new RuntimeException(String.format(ERR_ROLE_NOT_FOUND, request.role())));

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(role))
                .accountNonLocked(true)
                .failedAttempt(0)
                .lockCount(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info(LOG_ADMIN_CREATED_USER, request.email(), role.getName());

        return mapToAdminUserListResponse(savedUser);
    }

    public TokenResponse login(LoginRequest request, String clientIp) {
        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    ipLockService.registerFailedAttempt(clientIp);
                    return new BadCredentialsException(ERR_BAD_CREDENTIALS);
                });

        if (!user.isAccountNonLocked()) {
            long duration = getLockDuration(user.getLockCount());
            if (duration == -1L) throw new LockedException(ERR_ACCOUNT_LOCKED_PERM);

            if (shouldUnlock(user)) {
                unlockUser(user);
            } else {
                long timeLeft = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
                throw new LockedException(String.format(ERR_ACCOUNT_LOCKED_TEMP, timeLeft / 1000));
            }
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            resetAllAttempts(user);
            var accessToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user.getId());
            saveUserToken(user, accessToken);
            saveUserToken(user, refreshToken);
            return new TokenResponse(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            updateFailedAttempts(request.email());
            ipLockService.registerFailedAttempt(clientIp);
            throw e;
        }
    }

    @Transactional
    public TokenResponse refreshToken(String authHeader) {
        // 1. Validación básica del header
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        final String refreshToken = authHeader.substring(TOKEN_PREFIX.length());
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

            // 2. Verificar que el token existe en la DB y es válido
            var storedToken = tokenRepository.findByToken(refreshToken)
                    .orElse(null);

            if (storedToken != null && !storedToken.isExpired() && !storedToken.isRevoked()
                    && jwtService.isTokenValid(refreshToken, user)) {

                // 3. TOKEN ROTATION: Revocamos todos los anteriores para mayor seguridad
                revokeAllUserTokens(user.getId());

                // 4. Generamos el nuevo par (Access + Refresh)
                String accessToken = jwtService.generateToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                // 5. Persistimos los nuevos tokens
                saveUserToken(user, accessToken);
                saveUserToken(user, newRefreshToken);

                log.info("🔄 Token renovado con éxito para el usuario: {}", userEmail);

                return new TokenResponse(accessToken, newRefreshToken);
            }
        }

        log.warn("⚠️ Intento de refresh token fallido o token inválido");
        return null;
    }

    private void processFailedAttempt(User user) {
        int attempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(new Date());
            user.setLockCount(user.getLockCount() + 1);
            user.setFailedAttempt(0);
            log.warn(LOG_USER_LOCKED, user.getEmail(), user.getLockCount());
        }
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) return;

        tokenRepository.findByToken(authHeader.substring(7)).ifPresent(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
            SecurityContextHolder.clearContext();
            log.info(LOG_LOGOUT);
        });
    }

    @Transactional
    public TokenResponse resetPasswordFromProfile(String email, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        revokeAllUserTokens(user.getId());
        resetAllAttempts(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        userRepository.save(user);
        log.info(LOG_PASSWORD_UPDATED, email);
        return new TokenResponse(accessToken, refreshToken);
    }

    private AdminUserListResponse mapToAdminUserListResponse(User user) {
        return AdminUserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .accountNonLocked(user.isAccountNonLocked())
                .failedAttempt(user.getFailedAttempt())
                .build();
    }

    // --- Métodos de apoyo ---
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) { userRepository.findByEmail(email).ifPresent(this::processFailedAttempt); }

    @Transactional
    public void unlockUser(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    private boolean shouldUnlock(User user) {
        if (user.getLockTime() == null) return true;
        long duration = getLockDuration(user.getLockCount());
        return duration != -1L && user.getLockTime().getTime() + duration < System.currentTimeMillis();
    }

    private void resetAllAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private void saveUserToken(User user, String jwtToken) {
        tokenRepository.save(Token.builder().user(user).token(jwtToken).tokenType(Token.TokenType.BEARER).expired(false).revoked(false).build());
    }

    private void revokeAllUserTokens(Long userId) {
        var tokens = tokenRepository.findAllValidTokenByUser(userId);
        tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true); });
        tokenRepository.saveAll(tokens);
    }

    // --- Métodos de TOTP / 2FA ---

    /**
     * Configura TOTP para un usuario, generando el secreto y la URL del código QR
     */
    @Transactional
    public TotpSetupResponse setupTotp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        // Generar un nuevo secreto Base32
        String secret = twoFactorService.generateSecretBase32();
        
        // Guardar el secreto pero aún no habilitar TOTP
        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
        userRepository.save(user);

        // Generar la URL del código QR
        String qrCodeUrl = twoFactorService.generateQrCodeUrl(email, secret);

        log.info(LOG_TOTP_SETUP, email);
        return new TotpSetupResponse(secret, qrCodeUrl);
    }

    /**
     * Habilita TOTP después de verificar el código
     */
    @Transactional
    public void enableTotp(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        if (user.getTotpSecret() == null) {
            throw new IllegalStateException(ERR_TOTP_NOT_SETUP);
        }

        // Validar el código de 6 dígitos
        if (!twoFactorService.validateCode(user.getTotpSecret(), code)) {
            throw new BadCredentialsException(ERR_TOTP_INVALID_CODE);
        }

        user.setTotpEnabled(true);
        userRepository.save(user);
        log.info(LOG_TOTP_ENABLED, email);
    }

    /**
     * Deshabilita TOTP para un usuario
     */
    @Transactional
    public void disableTotp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
        log.info(LOG_TOTP_DISABLED, email);
    }

    /**
     * Verifica si TOTP está habilitado para un usuario
     */
    public TotpStatusResponse getTotpStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));
        return new TotpStatusResponse(user.isTotpEnabled());
    }

    /**
     * Login con TOTP
     */
    @Transactional
    public TokenResponse loginWithTotp(LoginWithTotpRequest request, String clientIp) {
        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    ipLockService.registerFailedAttempt(clientIp);
                    return new BadCredentialsException(ERR_BAD_CREDENTIALS);
                });

        if (!user.isAccountNonLocked()) {
            long duration = getLockDuration(user.getLockCount());
            if (duration == -1L) throw new LockedException(ERR_ACCOUNT_LOCKED_PERM);

            if (shouldUnlock(user)) {
                unlockUser(user);
            } else {
                long timeLeft = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
                throw new LockedException(String.format(ERR_ACCOUNT_LOCKED_TEMP, timeLeft / 1000));
            }
        }

        try {
            // Autenticar usuario y contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Si TOTP está habilitado, validar el código
            if (user.isTotpEnabled()) {
                if (request.totpCode() == null || !twoFactorService.validateCode(user.getTotpSecret(), request.totpCode())) {
                    ipLockService.registerFailedAttempt(clientIp);
                    throw new BadCredentialsException(ERR_TOTP_INVALID_CODE);
                }
            }

            resetAllAttempts(user);
            var accessToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user.getId());
            saveUserToken(user, accessToken);
            saveUserToken(user, refreshToken);
            return new TokenResponse(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            updateFailedAttempts(request.email());
            ipLockService.registerFailedAttempt(clientIp);
            throw e;
        }
    }
}