package com.fernandez.backend.application.service;

import com.fernandez.backend.shared.dto.AdminUpdateUserRequestDto;
import com.fernandez.backend.shared.dto.AdminUserListResponseDto;
import com.fernandez.backend.shared.dto.UpdateUserRequestDto;
import com.fernandez.backend.shared.dto.UserStatsResponseDto;
import com.fernandez.backend.domain.model.InvitationStatus;
import com.fernandez.backend.domain.model.Role;
import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.InvitationRepository;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.RoleRepository;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.UserRepository;
import com.fernandez.backend.application.port.in.IUserService;
import com.fernandez.backend.shared.constants.ServiceStrings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserStatsResponseDto getUserStatistics() {
        long total = userRepository.count();
        long blocked = userRepository.countByAccountNonLockedFalse();
        long pendingInvs = invitationRepository.countByStatus(InvitationStatus.PENDING);

        log.info(ServiceStrings.User.LOG_METRICS, total, blocked, pendingInvs);

        return new UserStatsResponseDto(total, blocked, pendingInvs);
    }

    @Override
    public List<AdminUserListResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toAdminUserListResponse).toList();
    }

    @Override
    public List<AdminUserListResponseDto> getLockedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isAccountNonLocked())
                .map(this::toAdminUserListResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unlockUser(String email) {
        User user = getUserByEmail(email);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setLockTime(null);
        log.info(ServiceStrings.User.LOG_ADMIN_UNLOCKED, email);
    }

    @Override
    @Transactional
    public void lockUser(String email) {
        User user = getUserByEmail(email);
        user.setAccountNonLocked(false);
        log.info(ServiceStrings.User.LOG_ADMIN_LOCKED, email);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = getUserById(id);
        if (isAdmin(user)) throw new RuntimeException(ServiceStrings.User.ERR_CANNOT_DELETE_ADMIN);
        userRepository.delete(user);
        log.info(ServiceStrings.User.LOG_ADMIN_DELETED, id);
    }

    @Override
    @Transactional
    public void updateUserRole(String email, String roleName) {
        User user = getUserByEmail(email);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_INVALID_ROLE_PREFIX + roleName));

        if (isAdmin(user) && !role.getName().equals("ADMIN")) {
            throw new RuntimeException(ServiceStrings.User.ERR_CANNOT_DEGRADE_ADMIN);
        }

        user.getRoles().clear();
        user.getRoles().add(role);
        log.info(ServiceStrings.User.LOG_ROLE_UPDATED, email, roleName);
    }

    @Override
    @Transactional
    public AdminUserListResponseDto updateUserByAdmin(Long id, AdminUpdateUserRequestDto request) {
        User user = getUserById(id);
        if (isAdmin(user)) throw new RuntimeException(ServiceStrings.User.ERR_CANNOT_MODIFY_ADMIN);

        if (request.name() != null) user.setName(request.name());
        if (request.accountNonLocked() != null) user.setAccountNonLocked(request.accountNonLocked());

        if (request.role() != null) {
            Role role = roleRepository.findByName(request.role())
                    .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_INVALID_ROLE_PREFIX));
            user.getRoles().clear();
            user.getRoles().add(role);
        }

        return toAdminUserListResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserListResponseDto getUserStatus(String email) {
        return toAdminUserListResponse(getUserByEmail(email));
    }

    @Override
    @Transactional
    public User updateMyProfile(String email, UpdateUserRequestDto request) {
        User user = getUserByEmail(email);
        user.setName(request.name());
        return userRepository.save(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_USER_NOT_FOUND_PREFIX + email));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_USER_NOT_FOUND_ID_PREFIX + id));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
    }

    private AdminUserListResponseDto toAdminUserListResponse(User user) {
        return new AdminUserListResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.isAccountNonLocked(),
                user.getFailedAttempt(),
                user.getLockCount()
        );
    }
}

