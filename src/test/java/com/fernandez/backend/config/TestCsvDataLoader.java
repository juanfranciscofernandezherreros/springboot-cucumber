package com.fernandez.backend.config;

import com.fernandez.backend.model.Invitation;
import com.fernandez.backend.model.InvitationStatus;
import com.fernandez.backend.model.Privilege;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import com.fernandez.backend.repository.InvitationRepository;
import com.fernandez.backend.repository.PrivilegeRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Carga datos de test desde CSV.
 * Se habilita exclusivamente en entorno de tests mediante la property:
 *   test.data.csv.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "test.data.csv.enabled", havingValue = "true", matchIfMissing = true)
public class TestCsvDataLoader {

    @Bean
    CommandLineRunner loadTestDataFromCsv(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PrivilegeRepository privilegeRepository,
            InvitationRepository invitationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            loadUsers(userRepository, roleRepository, privilegeRepository, passwordEncoder);
            loadInvitations(invitationRepository);
        };
    }

    private void loadUsers(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PrivilegeRepository privilegeRepository,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        ClassPathResource resource = new ClassPathResource("test-users-rbac.csv");
        if (!resource.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.trim().startsWith("#")) continue;

                String[] parts = splitCsvLine(line);
                if (parts.length < 5) continue;

                String email = parts[0].trim();
                String password = parts[1].trim();
                String name = parts[2].trim();
                String rolesRaw = parts[3].trim();
                String privilegesRaw = parts[4].trim();

                Set<Privilege> privileges = Arrays.stream(privilegesRaw.split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(p -> privilegeRepository.findByName(p).orElseGet(() -> {
                            Privilege pr = new Privilege();
                            pr.setName(p);
                            return privilegeRepository.save(pr);
                        }))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                Set<Role> roles = Arrays.stream(rolesRaw.split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(rn -> {
                            Role role = roleRepository.findByName(rn).orElseGet(() -> {
                                Role r = new Role();
                                r.setName(rn);
                                return r;
                            });
                            role.setPrivileges(privileges);
                            return roleRepository.save(role);
                        })
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                userRepository.findByEmail(email).orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setName(name);
                    u.setPassword(passwordEncoder.encode(password));
                    u.setRoles(roles);
                    u.setAccountNonLocked(true);
                    return userRepository.save(u);
                });
            }
        }
    }

    private void loadInvitations(InvitationRepository invitationRepository) throws Exception {
        ClassPathResource resource = new ClassPathResource("test-invitations.csv");
        if (!resource.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.trim().startsWith("#")) continue;

                String[] parts = splitCsvLine(line);
                if (parts.length < 8) continue;

                String email = parts[0].trim();
                String name = parts[1].trim();
                String description = parts[2].trim();
                String role = parts[3].trim();
                InvitationStatus status = InvitationStatus.valueOf(parts[4].trim());
                Instant createdAt = Instant.parse(parts[5].trim());
                Instant expiresAt = Instant.parse(parts[6].trim());
                String token = parts[7].trim();

                // Evitar duplicados: en el dominio actual hay unique(email, status)
                if (invitationRepository.existsByEmailAndStatus(email, status)) {
                    continue;
                }

                Invitation inv = Invitation.builder()
                        .email(email)
                        .name(name)
                        .description(description)
                        .role(role)
                        .status(status)
                        .createdAt(createdAt)
                        .expiresAt(expiresAt)
                        .token(token)
                        .build();

                invitationRepository.save(inv);
            }
        }
    }

    /**
     * Split mínimo de CSV con soporte básico de comillas dobles.
     */
    private static String[] splitCsvLine(String line) {
        if (!line.contains("\"")) {
            return line.split(",", -1);
        }

        StringBuilder current = new StringBuilder();
        java.util.List<String> out = new java.util.ArrayList<>();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        out.add(current.toString());
        return out.toArray(new String[0]);
    }
}
