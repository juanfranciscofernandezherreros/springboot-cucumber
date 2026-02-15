package com.fernandez.backend.config;

import com.fernandez.backend.repository.*;
import com.fernandez.backend.service.*;
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
        return new com.fernandez.backend.service.impl.JwtService();
    }

    @Bean
    public ILogoutService logoutService() {
        return new com.fernandez.backend.service.impl.LogoutService(tokenRepository);
    }

    @Bean
    public IIpLockService ipLockService() {
        return new com.fernandez.backend.service.impl.IpLockService(redisTemplate, ipLockProperties);
    }

    @Bean
    public IEmailService emailService() {
        return new com.fernandez.backend.service.impl.EmailService(mailSender);
    }

    @Bean
    public ITelegramService telegramService() {
        return new com.fernandez.backend.service.impl.TelegramService();
    }

    @Bean
    public IUserService userService() {
        return new com.fernandez.backend.service.impl.UserService(userRepository, invitationRepository, roleRepository);
    }

    @Bean
    public IAuditService auditService(EntityManager entityManager) {
        return new com.fernandez.backend.service.impl.AuditService(entityManager);
    }

    @Bean
    public IAuthService authService(PasswordEncoder passwordEncoder, IJwtService jwtService, AuthenticationManager authenticationManager, IIpLockService ipLockService, ITelegramService telegramService, IEmailService emailService) {
        return new com.fernandez.backend.service.impl.AuthService(userRepository, tokenRepository, passwordEncoder, jwtService, authenticationManager, ipLockService, roleRepository, telegramService, emailService, notificationProperties, lockProperties);
    }
}
