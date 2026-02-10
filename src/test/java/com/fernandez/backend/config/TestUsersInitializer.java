package com.fernandez.backend.config;

import com.fernandez.backend.repository.InvitationRepository;
import com.fernandez.backend.repository.PrivilegeRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        UserDataInitializer.init(
                userRepository,
                roleRepository,
                invitationRepository,
                privilegeRepository,
                passwordEncoder
        );
    }
}