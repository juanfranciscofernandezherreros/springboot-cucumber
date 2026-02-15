package com.fernandez.backend.web.controller;

import com.fernandez.backend.infrastructure.config.AdminMessagesProperties;
import com.fernandez.backend.shared.dto.*;
import com.fernandez.backend.application.port.in.IAuthService;
import com.fernandez.backend.application.port.in.IUserService;
import com.fernandez.backend.shared.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AdminController {

    private final IUserService userService;
    private final IAuthService authService;
    private final AdminMessagesProperties msg;

    // =====================================================
    // CREATE (admin:create)
    // =====================================================
    @PostMapping(ApiPaths.Admin.CREATE_USER)
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<AdminActionResponseDto<AdminUserListResponseDto>> createUserFromPanel(
            @RequestBody AdminCreateUserRequestDto request
    ) {
        AdminUserListResponseDto newUser = authService.registerByAdmin(request);

        String template = msg.getUserCreated();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AdminActionResponseDto.<AdminUserListResponseDto>builder()
                        .mensaje(String.format(template, request.email()))
                        .data(newUser)
                        .build()
        );
    }

    // =====================================================
    // READ (admin:read)
    // =====================================================
    @GetMapping(ApiPaths.Admin.USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AdminUserListResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(ApiPaths.Admin.LOCKED_USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AdminUserListResponseDto>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    @GetMapping(ApiPaths.Admin.USER_STATUS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<AdminUserListResponseDto> getUserStatus(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok(userService.getUserStatus(email));
    }

    @GetMapping(ApiPaths.Admin.STATS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<UserStatsResponseDto> getUserStats() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }

    // =====================================================
    // UPDATE (admin:update)
    // =====================================================
    @PostMapping(ApiPaths.Admin.LOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponseDto<AdminUserListResponseDto>> lockUser(@PathVariable(name = "email") String email) {
        userService.lockUser(email);
        AdminUserListResponseDto updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponseDto.<AdminUserListResponseDto>builder()
                        .mensaje(String.format(msg.getUserLocked(), email))
                        .data(updatedUser)
                        .build()
        );
    }

    @PostMapping(ApiPaths.Admin.UNLOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponseDto<AdminUserListResponseDto>> unLockUser(@PathVariable(name = "email") String email) {
        userService.unlockUser(email);
        AdminUserListResponseDto updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponseDto.<AdminUserListResponseDto>builder()
                        .mensaje(String.format(msg.getUserUnlocked(), email))
                        .data(updatedUser)
                        .build()
        );
    }

    @PutMapping(ApiPaths.Admin.UPDATE_ROLE)
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponseDto<AdminUserListResponseDto>> updateRole(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String roleName = request.get("role").toUpperCase();
        userService.updateUserRole(email, roleName);
        AdminUserListResponseDto updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponseDto.<AdminUserListResponseDto>builder()
                        .mensaje(String.format(msg.getRoleUpdated(), email, roleName))
                        .data(updatedUser)
                        .build()
        );
    }

    @PutMapping(ApiPaths.Admin.UPDATE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponseDto<AdminUserListResponseDto>> updateUser(
            @PathVariable(name = "id") Long id,
            @RequestBody AdminUpdateUserRequestDto request
    ) {
        AdminUserListResponseDto response = userService.updateUserByAdmin(id, request);

        return ResponseEntity.ok(
                AdminActionResponseDto.<AdminUserListResponseDto>builder()
                        .mensaje("Usuario actualizado correctamente") // Puedes añadirlo a Properties
                        .data(response)
                        .build()
        );
    }

    // =====================================================
    // DELETE (admin:delete)
    // =====================================================
    @DeleteMapping(ApiPaths.Admin.DELETE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<AdminActionResponseDto<Long>> deleteUser(@PathVariable(name = "id") Long id) {
        userService.deleteUserById(id);

        return ResponseEntity.ok(
                AdminActionResponseDto.<Long>builder()
                        .mensaje(String.format(msg.getUserDeleted(), id))
                        .data(id)
                        .build()
        );
    }
}