package com.fernandez.backend.controller;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;
import com.fernandez.backend.service.AuthService;
import com.fernandez.backend.service.UserService;
import com.fernandez.backend.utils.constants.UserApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(UserApiPaths.BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;
    private final AuthService service;

    @GetMapping(UserApiPaths.ME)
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AdminUserListResponse user =
                userService.getUserStatus(userDetails.getUsername());

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        return ResponseEntity.ok(response);
    }


    @PutMapping(UserApiPaths.UPDATE)
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        User updatedUser =
                userService.updateMyProfile(userDetails.getUsername(), request);

        UserResponse response = new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRoles().stream()
                        .map(role -> role.getName())
                        .toList()
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeMyPassword(
            @RequestBody ResetPasswordRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        TokenResponse tokens =
                service.resetPasswordFromProfile(email, request.newPassword());

        return ResponseEntity.ok(
                Map.of(
                        "mensaje", "Contraseña actualizada",
                        "access_token", tokens.accessToken(),
                        "refresh_token", tokens.refreshToken()
                )
        );
    }


}
