package com.fernandez.backend.service.impl;

import com.fernandez.backend.config.SecurityLockProperties;
import com.fernandez.backend.config.SecurityNotificationProperties;
import com.fernandez.backend.dto.*;
import com.fernandez.backend.exceptions.*;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.Token;
import com.fernandez.backend.model.User;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.TokenRepository;
import com.fernandez.backend.repository.UserRepository;
import com.fernandez.backend.service.*;
import com.fernandez.backend.utils.constants.ServiceStrings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

import static com.fernandez.backend.utils.constants.AuthServiceConstants.*;

@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IIpLockService ipLockService;
    private final RoleRepository roleRepository;
    private final ITelegramService telegramService;
    private final IEmailService emailService;
    private final SecurityNotificationProperties notificationProperties;
    private final SecurityLockProperties lockProperties;

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private long getLockDurationMillis(int lockCount) {
        // lockProperties devuelve duración en ms
        return lockProperties.getDurations().getOrDefault(lockCount, -1L);
    }

    @Override
    @Transactional
    public void registerPublic(RegisterRequestDto request, String clientIp) {
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

    @Override
    @Transactional
    public AdminUserListResponseDto registerByAdmin(AdminCreateUserRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(String.format(ERR_USER_ALREADY_EXISTS, request.email()));
        }

        Role role = roleRepository.findByName(request.role().toUpperCase())
                .orElseThrow(() -> new RuntimeException(String.format(ERR_ROLE_NOT_FOUND, request.role())));

        User user = User.builder()
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

    @Override
    public TokenResponseDto login(LoginRequest request, String clientIp) {
        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    ipLockService.registerFailedAttempt(clientIp);
                    return new BadCredentialsException(ERR_BAD_CREDENTIALS);
                });

        if (!user.isAccountNonLocked()) {
            long duration = getLockDurationMillis(user.getLockCount());
            if (duration == -1L) throw new LockedException(ERR_ACCOUNT_LOCKED_PERM);

            if (shouldUnlock(user, duration)) {
                unlockUser(user);
            } else {
                long timeLeftMs = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
                throw new LockedException(String.format(ERR_ACCOUNT_LOCKED_TEMP, Math.max(0, timeLeftMs / 1000)));
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            resetAllAttempts(user);

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user.getId());
            saveUserToken(user, accessToken);
            saveUserToken(user, refreshToken);

            return new TokenResponseDto(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            updateFailedAttempts(request.email());
            ipLockService.registerFailedAttempt(clientIp);
            throw e;
        }
    }

    @Override
    @Transactional
    public TokenResponseDto refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        final String refreshToken = authHeader.substring(TOKEN_PREFIX.length());
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            log.warn(ServiceStrings.Auth.REFRESH_FAILED);
            return null;
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        Token storedToken = tokenRepository.findByToken(refreshToken).orElse(null);

        if (storedToken == null || storedToken.isExpired() || storedToken.isRevoked()) {
            log.warn(ServiceStrings.Auth.REFRESH_FAILED);
            return null;
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            log.warn(ServiceStrings.Auth.REFRESH_FAILED);
            return null;
        }

        revokeAllUserTokens(user.getId());

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, newAccessToken);
        saveUserToken(user, newRefreshToken);

        log.info(ServiceStrings.Auth.TOKEN_REFRESHED, userEmail);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            return;
        }
        final String jwt = authHeader.substring(TOKEN_PREFIX.length());
        Token storedToken = tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }

    @Override
    @Transactional
    public TokenResponseDto resetPasswordFromProfile(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        revokeAllUserTokens(user.getId());
        resetAllAttempts(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        userRepository.save(user);
        log.info(LOG_PASSWORD_UPDATED, email);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(this::processFailedAttempt);
    }

    @Override
    @Transactional
    public void unlockUser(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    private void processFailedAttempt(User user) {
        int failedAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockCount(user.getLockCount() + 1);
            user.setLockTime(new Date());
        }

        userRepository.save(user);
    }

    private boolean shouldUnlock(User user, long durationMillis) {
        if (user.getLockTime() == null) return true;
        return user.getLockTime().getTime() + durationMillis < System.currentTimeMillis();
    }

    private void resetAllAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private void saveUserToken(User user, String jwtToken) {
        tokenRepository.save(Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build());
    }

    private void revokeAllUserTokens(Long userId) {
        var tokens = tokenRepository.findAllValidTokenByUser(userId);
        tokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    private AdminUserListResponseDto mapToAdminUserListResponse(User user) {
        return new AdminUserListResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.isAccountNonLocked(),
                user.getFailedAttempt(),
                user.getLockCount()
        );
    }
}
