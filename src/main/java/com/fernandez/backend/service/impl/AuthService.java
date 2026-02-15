package com.fernandez.backend.service.impl;

}
    }
        );
                user.getLockCount()
                user.getFailedAttempt(),
                user.isAccountNonLocked(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.getEmail(),
                user.getName(),
                user.getId(),
        return new AdminUserListResponseDto(
    private AdminUserListResponseDto mapToAdminUserListResponse(User user) {

    }
        tokenRepository.saveAll(tokens);
        });
            t.setRevoked(true);
            t.setExpired(true);
        tokens.forEach(t -> {
        var tokens = tokenRepository.findAllValidTokenByUser(userId);
    private void revokeAllUserTokens(Long userId) {

    }
        tokenRepository.save(Token.builder().user(user).token(jwtToken).tokenType(Token.TokenType.BEARER).expired(false).revoked(false).build());
    private void saveUserToken(User user, String jwtToken) {

    }
        userRepository.save(user);
        user.setLockTime(null);
        user.setAccountNonLocked(true);
        user.setLockCount(0);
        user.setFailedAttempt(0);
    private void resetAllAttempts(User user) {

    }
        return duration != -1L && user.getLockTime().getTime() + duration < System.currentTimeMillis();
        long duration = getLockDuration(user.getLockCount());
        if (user.getLockTime() == null) return true;
    private boolean shouldUnlock(User user) {

    }
        userRepository.save(user);

        }
            user.setLockTime(new java.util.Date());
            user.setLockCount(user.getLockCount() + 1);
            user.setAccountNonLocked(false);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {

        user.setFailedAttempt(failedAttempts);
        int failedAttempts = user.getFailedAttempt() + 1;
    private void processFailedAttempt(User user) {

    }
        userRepository.save(user);
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
    public void unlockUser(User user) {
    @Transactional
    @Override

    }
        userRepository.findByEmail(email).ifPresent(this::processFailedAttempt);
    public void updateFailedAttempts(String email) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override

    }
        return new TokenResponseDto(accessToken, refreshToken);
        log.info(LOG_PASSWORD_UPDATED, email);
        userRepository.save(user);

        saveUserToken(user, refreshToken);
        saveUserToken(user, accessToken);
        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken = jwtService.generateToken(user);

        resetAllAttempts(user);
        revokeAllUserTokens(user.getId());
        user.setPassword(passwordEncoder.encode(newPassword));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));
    public TokenResponseDto resetPasswordFromProfile(String email, String newPassword) {
    @Transactional
    @Override

    }
        }
            tokenRepository.save(storedToken);
            storedToken.setRevoked(true);
            storedToken.setExpired(true);
        if (storedToken != null) {
        var storedToken = tokenRepository.findByToken(jwt).orElse(null);
        final String jwt = authHeader.substring(TOKEN_PREFIX.length());
        }
            return;
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
    public void logout(String authHeader) {
    @Transactional
    @Override

    }
        return null;
        log.warn(ServiceStrings.Auth.REFRESH_FAILED);

        }
            }
                return new TokenResponseDto(accessToken, newRefreshToken);

                log.info(ServiceStrings.Auth.TOKEN_REFRESHED, userEmail);

                saveUserToken(user, newRefreshToken);
                saveUserToken(user, accessToken);

                String newRefreshToken = jwtService.generateRefreshToken(user);
                String accessToken = jwtService.generateToken(user);

                revokeAllUserTokens(user.getId());

                    && jwtService.isTokenValid(refreshToken, user)) {
            if (storedToken != null && !storedToken.isExpired() && !storedToken.isRevoked()

                    .orElse(null);
            var storedToken = tokenRepository.findByToken(refreshToken)

                    .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));
            var user = userRepository.findByEmail(userEmail)
        if (userEmail != null) {

        final String userEmail = jwtService.extractUsername(refreshToken);
        final String refreshToken = authHeader.substring(TOKEN_PREFIX.length());

        }
            return null;
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
    public TokenResponseDto refreshToken(String authHeader) {
    @Transactional
    @Override

    }
        }
            throw e;
            ipLockService.registerFailedAttempt(clientIp);
            updateFailedAttempts(request.email());
        } catch (BadCredentialsException e) {
            return new TokenResponseDto(accessToken, refreshToken);
            saveUserToken(user, refreshToken);
            saveUserToken(user, accessToken);
            revokeAllUserTokens(user.getId());
            var refreshToken = jwtService.generateRefreshToken(user);
            var accessToken = jwtService.generateToken(user);
            resetAllAttempts(user);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        try {

        }
            }
                throw new LockedException(String.format(ERR_ACCOUNT_LOCKED_TEMP, timeLeft / 1000));
                long timeLeft = (user.getLockTime().getTime() + duration) - System.currentTimeMillis();
            } else {
                unlockUser(user);
            if (shouldUnlock(user)) {

            if (duration == -1L) throw new LockedException(ERR_ACCOUNT_LOCKED_PERM);
            long duration = getLockDuration(user.getLockCount());
        if (!user.isAccountNonLocked()) {

                });
                    return new BadCredentialsException(ERR_BAD_CREDENTIALS);
                    ipLockService.registerFailedAttempt(clientIp);
                .orElseThrow(() -> {
        var user = userRepository.findByEmail(request.email())

        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();
    public TokenResponseDto login(LoginRequest request, String clientIp) {
    @Override

    }
        return mapToAdminUserListResponse(savedUser);

        log.info(LOG_ADMIN_CREATED_USER, request.email(), role.getName());
        User savedUser = userRepository.save(user);

                .build();
                .lockCount(0)
                .failedAttempt(0)
                .accountNonLocked(true)
                .roles(Set.of(role))
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .name(request.name())
        var user = User.builder()

                .orElseThrow(() -> new RuntimeException(String.format(ERR_ROLE_NOT_FOUND, request.role())));
        Role role = roleRepository.findByName(request.role().toUpperCase())

        }
            throw new UserAlreadyExistsException(String.format(ERR_USER_ALREADY_EXISTS, request.email()));
        if (userRepository.existsByEmail(request.email())) {
    public AdminUserListResponseDto registerByAdmin(AdminCreateUserRequestDto request) {
    @Transactional
    @Override

    }
        }
            telegramService.sendMessage(String.format(TELEGRAM_MSG_NEW_USER, request.name(), request.email(), clientIp));
        if (notificationProperties.isSendDataTelegramEnabled()) {

        }
            emailService.sendEmail(user.getEmail(), EMAIL_SUBJECT_WELCOME, String.format(EMAIL_BODY_WELCOME, user.getName()));
        if (notificationProperties.isSendDataEmailEnabled()) {

        userRepository.save(user);

                .build();
                .lockCount(0)
                .failedAttempt(0)
                .accountNonLocked(true)
                .roles(Set.of(userRole))
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .name(request.name())
        User user = User.builder()

                .orElseThrow(() -> new IllegalStateException(String.format(ERR_ROLE_NOT_FOUND, ROLE_USER)));
        Role userRole = roleRepository.findByName(ROLE_USER)

        if (userRepository.existsByEmail(request.email())) throw new EmailAlreadyExistsException(ERR_EMAIL_EXISTS);

        }
            if (!ROLE_USER.equalsIgnoreCase(request.role())) throw new InvalidRoleException(ERR_INVALID_REG_ROLE);
            if (ROLE_ADMIN.equalsIgnoreCase(request.role())) throw new ForbiddenRoleException(ERR_FORBIDDEN_ADMIN_REG);
        if (request.role() != null) {

        if (ipLockService.isIpBlocked(clientIp)) throw new IpBlockedException();
    public void registerPublic(RegisterRequestDto request, String clientIp) {
    @Transactional
    @Override

    }
        return lockProperties.getDurations().getOrDefault(lockCount, -1L);
    private long getLockDuration(int lockCount) {

    private final SecurityLockProperties lockProperties;
    private final SecurityNotificationProperties notificationProperties;
    private final IEmailService emailService;
    private final ITelegramService telegramService;
    private final RoleRepository roleRepository;
    private final IIpLockService ipLockService;
    private final AuthenticationManager authenticationManager;
    private final IJwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

public class AuthService implements IAuthService {
@Slf4j
@RequiredArgsConstructor

import static com.fernandez.backend.utils.constants.AuthServiceConstants.*;

import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.fernandez.backend.utils.constants.ServiceStrings;
import com.fernandez.backend.service.*;
import com.fernandez.backend.repository.UserRepository;
import com.fernandez.backend.repository.TokenRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.model.User;
import com.fernandez.backend.model.Token;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.exceptions.*;
import com.fernandez.backend.dto.*;
import com.fernandez.backend.config.SecurityNotificationProperties;
import com.fernandez.backend.config.SecurityLockProperties;

