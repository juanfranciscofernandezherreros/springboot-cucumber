package com.fernandez.backend.config;

import com.fernandez.backend.model.Invitation;
import com.fernandez.backend.model.InvitationStatus;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import com.fernandez.backend.repository.InvitationRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

public class UserDataInitializer {

    public static void init(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InvitationRepository invitationRepository, // Inyectamos el repositorio
            PasswordEncoder passwordEncoder
    ) {
        // --- INICIALIZACIÓN DE ROLES ---
        createRoleIfNotExists(roleRepository, "ADMIN");
        createRoleIfNotExists(roleRepository, "MANAGER");
        createRoleIfNotExists(roleRepository, "USER");

        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role managerRole = roleRepository.findByName("MANAGER").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        // --- INICIALIZACIÓN DE USUARIOS ---
        createUser(userRepository, passwordEncoder, "admin@test.com", "Super Admin", "admin123", Set.of(adminRole), false);
        createUser(userRepository, passwordEncoder, "user2@test.com", "User Active", "user123", Set.of(userRole), false);
        createUser(userRepository, passwordEncoder, "user3@test.com", "User INactive", "user123", Set.of(userRole), true);
        createUser(userRepository, passwordEncoder, "user4@test.com", "User Active", "user123", Set.of(userRole), false);
        // --- INICIALIZACIÓN DE INVITACIONES (Varios estados) ---

        // 1. Invitaciones PENDIENTES (Aparecerán en /pending y /all)
        createInvitation(invitationRepository, "pendiente1@test.com", "Invitado Uno", "Interés en departamento IT", InvitationStatus.PENDING);
        createInvitation(invitationRepository, "pendiente2@test.com", "Invitado Dos", "Candidato RRHH", InvitationStatus.PENDING);

        // 2. Invitaciones ACEPTADAS (Historial)
        createInvitation(invitationRepository, "aceptada@test.com", "Juan Aceptado", "Antiguo empleado", InvitationStatus.ACCEPTED);

        // 3. Invitaciones RECHAZADAS (Historial)
        createInvitation(invitationRepository, "rechazada@test.com", "Luis Rechazado", "No cumple requisitos", InvitationStatus.REJECTED);

        // 4. Invitación EXPIRADA (Historial)
        createInvitation(invitationRepository, "expirada@test.com", "Maria Expirada", "Nunca respondió", InvitationStatus.EXPIRED);
    }

    private static void createInvitation(
            InvitationRepository repo,
            String email,
            String name,
            String desc,
            InvitationStatus status
    ) {
        if (!repo.existsByEmailAndStatus(email, status)) {
            Invitation invitation = Invitation.builder()
                    .email(email)
                    .name(name)
                    .description(desc)
                    .role("USER")
                    .token(UUID.randomUUID().toString())
                    .status(status)
                    .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                    .expiresAt(Instant.now().plus(2, ChronoUnit.DAYS))
                    .build();
            repo.save(invitation);
        }
    }

    private static void createUser(UserRepository repo, PasswordEncoder encoder, String email, String name, String rawPass, Set<Role> roles, boolean isLocked) {
        if (repo.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(encoder.encode(rawPass));
            user.setRoles(roles);
            user.setAccountNonLocked(!isLocked);
            repo.save(user);
        }
    }

    private static void createRoleIfNotExists(RoleRepository repo, String roleName) {
        if (!repo.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            repo.save(role);
        }
    }
}