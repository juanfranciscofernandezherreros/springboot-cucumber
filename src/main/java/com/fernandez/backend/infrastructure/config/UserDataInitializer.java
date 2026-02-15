package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.domain.model.*;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.*;
import com.fernandez.backend.shared.constants.OperationMessageKeys;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

public class UserDataInitializer {

    public static void init(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InvitationRepository invitationRepository,
            PrivilegeRepository privilegeRepository,
            PasswordEncoder passwordEncoder,
            OperationMessageRepository operationMessageRepository
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
        Role limitedAdminRole = createRoleWithPrivileges(roleRepository, "LIMITED_ADMIN",
                Set.of(viewHd, adminRead, adminUpdate, adminCreate));
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
        createUser(userRepository, passwordEncoder, "limited-admin@test.com", "Admin Limitado", "admin123", Set.of(limitedAdminRole), false);
        // --- 4. INVITACIONES ---
        createInvitation(invitationRepository, "invitado@test.com", "Nuevo Cinefilo", "Interesado en catálogo de terror", InvitationStatus.PENDING);
        // Invitación para user3 (para que el test pueda capturar el ID si lo necesitas desde ahí)
        createInvitation(invitationRepository, "user3@test.com", "Usuario Tres", "Estado inicial", InvitationStatus.ACCEPTED);

        // --- 5. MENSAJES OPERACIONALES (i18n) ---
        seedOperationMessages(operationMessageRepository);
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

    private static void seedOperationMessages(OperationMessageRepository repo) {
        // Español
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_CREATED, "es", "Usuario %s creado correctamente");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_LOCKED, "es", "Usuario %s ha sido bloqueado correctamente.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UNLOCKED, "es", "Usuario %s ha sido desbloqueado correctamente.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_ROLE_UPDATED, "es", "Rol de %s actualizado a %s");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UPDATED, "es", "Usuario actualizado correctamente");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_DELETED, "es", "Usuario con id %d eliminado correctamente.");
        upsertMessage(repo, OperationMessageKeys.USER_PASSWORD_CHANGED, "es", "Contraseña actualizada");

        // Inglés
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_CREATED, "en", "User %s created successfully");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_LOCKED, "en", "User %s has been locked successfully.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UNLOCKED, "en", "User %s has been unlocked successfully.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_ROLE_UPDATED, "en", "Role for %s updated to %s");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UPDATED, "en", "User updated successfully");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_DELETED, "en", "User with id %d deleted successfully.");
        upsertMessage(repo, OperationMessageKeys.USER_PASSWORD_CHANGED, "en", "Password updated");

        // Catalán
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_CREATED, "ca", "Usuari %s creat correctament");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_LOCKED, "ca", "Usuari %s bloquejat correctament.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UNLOCKED, "ca", "Usuari %s desbloquejat correctament.");
        upsertMessage(repo, OperationMessageKeys.ADMIN_ROLE_UPDATED, "ca", "Rol de %s actualitzat a %s");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_UPDATED, "ca", "Usuari actualitzat correctament");
        upsertMessage(repo, OperationMessageKeys.ADMIN_USER_DELETED, "ca", "Usuari amb id %d eliminat correctament.");
        upsertMessage(repo, OperationMessageKeys.USER_PASSWORD_CHANGED, "ca", "Contrasenya actualitzada");
    }

    private static void upsertMessage(OperationMessageRepository repo, String key, String lang, String text) {
        repo.findByKeyAndLang(key, lang)
                .or(() -> java.util.Optional.of(new OperationMessage(key, lang, text)))
                .ifPresent(m -> {
                    m.setText(text);
                    repo.save(m);
                });
    }
}