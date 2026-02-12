package com.fernandez.backend.controller;

import com.fernandez.backend.config.AdminMessagesProperties;
import com.fernandez.backend.dto.*;
import com.fernandez.backend.service.AuthService;
import com.fernandez.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fernandez.backend.utils.constants.AdminApiPaths.*;

@RestController
@RequestMapping(BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;
    private final AdminMessagesProperties msg;

    // =====================================================
    // CREATE (admin:create)
    // =====================================================
    @PostMapping(CREATE_USER)
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<AdminActionResponse<AdminUserListResponse>> createUserFromPanel(
            @RequestBody AdminCreateUserRequest request
    ) {
        AdminUserListResponse newUser = authService.registerByAdmin(request);

        String template = msg.getUserCreated();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AdminActionResponse.<AdminUserListResponse>builder()
                        .mensaje(String.format(template, request.email()))
                        .data(newUser)
                        .build()
        );
    }

    // =====================================================
    // READ (admin:read)
    // =====================================================
    @GetMapping(USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AdminUserListResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(LOCKED_USERS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AdminUserListResponse>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    @GetMapping(USER_STATUS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<AdminUserListResponse> getUserStatus(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserStatus(email));
    }

    @GetMapping(STATS)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }

    // =====================================================
    // UPDATE (admin:update)
    // =====================================================
    @PostMapping(LOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponse<AdminUserListResponse>> lockUser(@PathVariable String email) {
        userService.lockUser(email);
        AdminUserListResponse updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponse.<AdminUserListResponse>builder()
                        .mensaje(String.format(msg.getUserLocked(), email))
                        .data(updatedUser)
                        .build()
        );
    }

    @PostMapping(UNLOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponse<AdminUserListResponse>> unLockUser(@PathVariable String email) {
        userService.unlockUser(email);
        AdminUserListResponse updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponse.<AdminUserListResponse>builder()
                        .mensaje(String.format(msg.getUserUnlocked(), email))
                        .data(updatedUser)
                        .build()
        );
    }

    @PutMapping(UPDATE_ROLE)
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponse<AdminUserListResponse>> updateRole(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String roleName = request.get("role").toUpperCase();
        userService.updateUserRole(email, roleName);
        AdminUserListResponse updatedUser = userService.getUserStatus(email);

        return ResponseEntity.ok(
                AdminActionResponse.<AdminUserListResponse>builder()
                        .mensaje(String.format(msg.getRoleUpdated(), email, roleName))
                        .data(updatedUser)
                        .build()
        );
    }

    @PutMapping(UPDATE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminActionResponse<AdminUserListResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request
    ) {
        AdminUserListResponse response = userService.updateUserByAdmin(id, request);

        return ResponseEntity.ok(
                AdminActionResponse.<AdminUserListResponse>builder()
                        .mensaje("Usuario actualizado correctamente") // Puedes añadirlo a Properties
                        .data(response)
                        .build()
        );
    }

    // =====================================================
    // DELETE (admin:delete)
    // =====================================================
    @DeleteMapping(DELETE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<AdminActionResponse<Long>> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);

        return ResponseEntity.ok(
                AdminActionResponse.<Long>builder()
                        .mensaje(String.format(msg.getUserDeleted(), id))
                        .data(id)
                        .build()
        );
    }
}