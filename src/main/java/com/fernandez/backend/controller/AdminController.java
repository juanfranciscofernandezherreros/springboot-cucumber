package com.fernandez.backend.controller;

import com.fernandez.backend.dto.AdminCreateUserRequest;
import com.fernandez.backend.dto.AdminUpdateUserRequest;
import com.fernandez.backend.dto.AdminUserListResponse;
import com.fernandez.backend.dto.UserStatsResponse;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.service.AuthService;
import com.fernandez.backend.model.User;
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

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping(CREATE_USER)
    public ResponseEntity<Void> createUserFromPanel(
            @RequestBody AdminCreateUserRequest request
    ) {
        authService.registerByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =====================================================
    // READ
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

    // =====================================================
    // UPDATE
    // =====================================================
    @PostMapping(UNLOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> unLockUser(
            @PathVariable String email
    ) {
        userService.unlockUser(email);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " ha sido desbloqueado correctamente.")
        );
    }


    // =====================================================
    // UPDATE
    // =====================================================
    @PostMapping(LOCK_USER + "/{email}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<Map<String, String>> lockUser(
            @PathVariable String email
    ) {
        userService.lockUser(email);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario " + email + " ha sido bloqueado correctamente.")
        );
    }

    @PutMapping(UPDATE_ROLE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateRole(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String roleName = request.get("role").toUpperCase();

        userService.updateUserRole(email, roleName);

        return ResponseEntity.ok(
                Map.of("mensaje", "Rol de " + email + " actualizado a " + roleName)
        );
    }


    // =====================================================
    // DELETE
    // =====================================================
    // =====================================================
// DELETE
// =====================================================
    @DeleteMapping(DELETE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id) {

        userService.deleteUserById(id);

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario con id " + id + " eliminado correctamente.")
        );
    }


    @PutMapping(UPDATE_USER + "/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<AdminUserListResponse> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request
    ) {
        AdminUserListResponse response = userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(STATS)
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }


}
