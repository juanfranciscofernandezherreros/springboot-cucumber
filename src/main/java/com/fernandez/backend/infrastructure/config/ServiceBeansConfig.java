package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.application.port.in.*;
import com.fernandez.backend.application.service.*;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.InvitationRepository;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.RoleRepository;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.TokenRepository;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ServiceBeansConfig {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final InvitationRepository invitationRepository;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final IpLockProperties ipLockProperties;
    private final SecurityNotificationProperties notificationProperties;
    private final SecurityLockProperties lockProperties;

    @Bean
    public IJwtService jwtService() {
        return new JwtService();
    }

    @Bean
    public ILogoutService logoutService() {
        return new LogoutService(tokenRepository);
    }

    @Bean
    public IIpLockService ipLockService() {
        return new IpLockService(redisTemplate, ipLockProperties);
    }

    @Bean
    public IEmailService emailService() {
        return new EmailService(mailSender);
    }

    @Bean
    public ITelegramService telegramService() {
        return new TelegramService();
    }

    @Bean
    public IUserService userService() {
        return new UserService(userRepository, invitationRepository, roleRepository);
    }

    @Bean
    public IAuditService auditService(EntityManager entityManager) {
        return new AuditService(entityManager);
    }

    @Bean
    public IAuthService authService(PasswordEncoder passwordEncoder, IJwtService jwtService, AuthenticationManager authenticationManager, IIpLockService ipLockService, ITelegramService telegramService, IEmailService emailService) {
        return new AuthService(userRepository, tokenRepository, passwordEncoder, jwtService, authenticationManager, ipLockService, roleRepository, telegramService, emailService, notificationProperties, lockProperties);
    }
}
