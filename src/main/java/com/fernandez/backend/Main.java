package com.fernandez.backend;

import com.fernandez.backend.config.UserDataInitializer;
import com.fernandez.backend.repository.InvitationRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InvitationRepository invitationRepository, // Agregado para las invitaciones
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("🚀 Inicializando datos base (DEV)");

            // Ahora pasamos el 4º argumento al Initializer
            UserDataInitializer.init(
                    userRepository,
                    roleRepository,
                    invitationRepository,
                    passwordEncoder
            );

            log.info("✅ Datos base inicializados (DEV)");
        };
    }
}