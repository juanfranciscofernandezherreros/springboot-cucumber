package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.infrastructure.persistence.jpa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeedConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InvitationRepository invitationRepository;
    private final PrivilegeRepository privilegeRepository;
    private final OperationMessageRepository operationMessageRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedDatabase() {
        return args -> UserDataInitializer.init(
                userRepository,
                roleRepository,
                invitationRepository,
                privilegeRepository,
                passwordEncoder,
                operationMessageRepository
        );
    }
}

