package com.fernandez.backend.config;

import com.fernandez.backend.model.*;
import com.fernandez.backend.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserDataInitializer {

    public static void init(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InvitationRepository invitationRepository,
            PrivilegeRepository privilegeRepository,
            PasswordEncoder passwordEncoder
    ) {
        // --- 1. PRIVILEGIOS ---
        Privilege view4k = createPrivilegeIfNotExist(privilegeRepository, "movie:stream:4k");
        Privilege viewHd = createPrivilegeIfNotExist(privilegeRepository, "movie:stream:hd");
        Privilege download = createPrivilegeIfNotExist(privilegeRepository, "movie:download");
        Privilege upload = createPrivilegeIfNotExist(privilegeRepository, "movie:upload");
        Privilege accessJava = createPrivilegeIfNotExist(privilegeRepository, "course:access:java");

        Privilege adminRead = createPrivilegeIfNotExist(privilegeRepository, "admin:read");
        Privilege adminUpdate = createPrivilegeIfNotExist(privilegeRepository, "admin:update");
        Privilege adminDelete = createPrivilegeIfNotExist(privilegeRepository, "admin:delete");
        Privilege adminCreate = createPrivilegeIfNotExist(privilegeRepository, "admin:create");

        // --- 2. ROLES ---
        Role adminRole = createRoleWithPrivileges(roleRepository, "ADMIN",
                Set.of(view4k, viewHd, download, upload, accessJava, adminRead, adminUpdate, adminDelete, adminCreate));

        // Creamos el rol MANAGER (requerido por tu test de actualización de rol)
        Role managerRole = createRoleWithPrivileges(roleRepository, "MANAGER",
                Set.of(viewHd, adminRead, adminUpdate));

        Role premiumRole = createRoleWithPrivileges(roleRepository, "PREMIUM",
                Set.of(view4k, viewHd, download, accessJava));

        Role userRole = createRoleWithPrivileges(roleRepository, "USER",
                Set.of(viewHd, accessJava));

        // --- 3. USUARIOS PARA ESCENARIOS DE TEST ---

        // Background: Login de Super Admin
        createUser(userRepository, passwordEncoder, "admin@test.com", "Admin Master", "admin123", Set.of(adminRole), false);

        // Scenario: Bloquear/Desbloquear/Rol (Necesita a user2)
        createUser(userRepository, passwordEncoder, "user2@test.com", "Usuario Dos", "user123", Set.of(userRole), false);

        // Scenario: Actualizar y Eliminar (Necesita a user3)
        createUser(userRepository, passwordEncoder, "user3@test.com", "Usuario Tres", "user123", Set.of(userRole), false);
        createUser(userRepository, passwordEncoder, "user4@test.com", "Usuario Cuatro", "user123", Set.of(userRole), false);

        // Scenario: Listar usuarios (Busca a admin_created)
        createUser(userRepository, passwordEncoder, "admin_created@test.com", "Admin Creado Test", "password123", Set.of(userRole), false);

        // Otros usuarios base
        createUser(userRepository, passwordEncoder, "premium@test.com", "Juan Premium", "user123", Set.of(premiumRole), false);
        createUser(userRepository, passwordEncoder, "alumno@test.com", "Pedro Java", "user123", Set.of(userRole), false);

        // --- 4. INVITACIONES ---
        createInvitation(invitationRepository, "invitado@test.com", "Nuevo Cinefilo", "Interesado en catálogo de terror", InvitationStatus.PENDING);
        // Invitación para user3 (para que el test pueda capturar el ID si lo necesitas desde ahí)
        createInvitation(invitationRepository, "user3@test.com", "Usuario Tres", "Estado inicial", InvitationStatus.ACCEPTED);
    }

    /* MÉTODOS AUXILIARES */
    private static Privilege createPrivilegeIfNotExist(PrivilegeRepository repo, String name) {
        return repo.findByName(name).orElseGet(() -> {
            Privilege p = new Privilege();
            p.setName(name);
            return repo.save(p);
        });
    }

    private static Role createRoleWithPrivileges(RoleRepository repo, String name, Set<Privilege> privileges) {
        Role role = repo.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return r;
        });
        role.setPrivileges(privileges);
        return repo.save(role);
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

    private static void createInvitation(InvitationRepository repo, String email, String name, String desc, InvitationStatus status) {
        if (!repo.existsByEmailAndStatus(email, status)) {
            Invitation invitation = Invitation.builder()
                    .email(email)
                    .name(name)
                    .description(desc)
                    .role("USER")
                    .token(UUID.randomUUID().toString())
                    .status(status)
                    .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                    .expiresAt(Instant.now().plus(5, ChronoUnit.DAYS))
                    .build();
            repo.save(invitation);
        }
    }
}