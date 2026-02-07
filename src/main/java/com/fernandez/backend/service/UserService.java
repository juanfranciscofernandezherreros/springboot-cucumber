package com.fernandez.backend.service;

import com.fernandez.backend.dto.AdminUpdateUserRequest;
import com.fernandez.backend.dto.AdminUserListResponse;
import com.fernandez.backend.dto.UpdateUserRequest;
import com.fernandez.backend.dto.UserStatsResponse;
import com.fernandez.backend.model.InvitationStatus;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import com.fernandez.backend.repository.InvitationRepository;
import com.fernandez.backend.repository.RoleRepository;
import com.fernandez.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;

    // =====================================================
    // MÉTRICAS
    // =====================================================
    public UserStatsResponse getUserStatistics() {
        long total = userRepository.count();
        long blocked = userRepository.countByAccountNonLockedFalse();
        long pendingInvs = invitationRepository.countByStatus(InvitationStatus.PENDING);

        log.info("MÉTRICAS: Total={}, Bloqueados={}, InvitacionesPendientes={}",
                total, blocked, pendingInvs);

        return new UserStatsResponse(total, blocked, pendingInvs);
    }

    // =====================================================
    // LISTADOS (ADMIN)
    // =====================================================
    public List<AdminUserListResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminUserListResponse)
                .toList();
    }

    public List<AdminUserListResponse> getLockedUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.isAccountNonLocked())
                .map(this::toAdminUserListResponse)
                .toList();
    }

    // =====================================================
    // BLOQUEO / DESBLOQUEO
    // =====================================================
    @Transactional
    public void unlockUser(String email) {
        User user = getUserByEmail(email);

        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setLockTime(null);

        log.info("ADMIN: Usuario {} desbloqueado", email);
    }

    @Transactional
    public void lockUser(String email) {
        User user = getUserByEmail(email);
        user.setAccountNonLocked(false);

        log.info("ADMIN: Usuario {} bloqueado", email);
    }

    // =====================================================
    // ELIMINACIÓN / MODIFICACIÓN (ADMIN)
    // =====================================================
    @Transactional
    public void deleteUserById(Long id) {
        User user = getUserById(id);

        if (isAdmin(user)) {
            throw new RuntimeException("No está permitido eliminar a otros administradores.");
        }

        userRepository.delete(user);
        log.info("ADMIN: Usuario con id {} eliminado", id);
    }

    @Transactional
    public void updateUserRole(String email, String roleName) {
        User user = getUserByEmail(email);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new RuntimeException("Rol no válido: " + roleName));

        if (isAdmin(user) && !role.getName().equals("ADMIN")) {
            throw new RuntimeException("No puedes degradar a otro ADMIN.");
        }

        user.getRoles().clear();
        user.getRoles().add(role);

        log.info("ADMIN: Rol de {} actualizado a {}", email, roleName);
    }

    @Transactional
    public AdminUserListResponse updateUserByAdmin(Long id, AdminUpdateUserRequest request) {
        User user = getUserById(id);

        if (isAdmin(user)) {
            throw new RuntimeException("No está permitido modificar a otros administradores.");
        }

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.accountNonLocked() != null) {
            user.setAccountNonLocked(request.accountNonLocked());
        }

        if (request.role() != null) {
            Role role = roleRepository.findByName(request.role())
                    .orElseThrow(() ->
                            new RuntimeException("Rol no válido"));

            user.getRoles().clear();
            user.getRoles().add(role);
        }

        return toAdminUserListResponse(user);
    }

    // =====================================================
    // PERFIL / ESTADO DE USUARIO
    // =====================================================
    public AdminUserListResponse getUserStatus(String email) {
        return toAdminUserListResponse(getUserByEmail(email));
    }

    @Transactional
    public User updateMyProfile(String email, UpdateUserRequest request) {
        User user = getUserByEmail(email);

        if (request.name() != null) {
            user.setName(request.name());
        }

        return user;
    }

    // =====================================================
    // HELPERS PRIVADOS
    // =====================================================
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado: " + email)
                );
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado con id: " + id)
                );
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }

    private AdminUserListResponse toAdminUserListResponse(User user) {
        return AdminUserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(
                        user.getRoles().stream()
                                .map(Role::getName)
                                .toList()
                )
                .accountNonLocked(user.isAccountNonLocked())
                .failedAttempt(user.getFailedAttempt())
                .build();
    }
}
