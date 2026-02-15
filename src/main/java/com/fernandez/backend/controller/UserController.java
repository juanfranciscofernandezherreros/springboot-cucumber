package com.fernandez.backend.controller;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;
import com.fernandez.backend.service.IAuthService;
import com.fernandez.backend.service.IUserService;
import com.fernandez.backend.utils.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Users.BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final IUserService userService;
    private final IAuthService service;

    @GetMapping(ApiPaths.Users.ME)
    public ResponseEntity<UserResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AdminUserListResponseDto user =
                userService.getUserStatus(userDetails.getUsername());

        UserResponseDto response = new UserResponseDto(
                user.id(),
                user.name(),
                user.email(),
                user.roles()
        );

        return ResponseEntity.ok(response);
    }


    @PutMapping(ApiPaths.Users.UPDATE)
    public ResponseEntity<UserResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequestDto request
    ) {
        User updatedUser =
                userService.updateMyProfile(userDetails.getUsername(), request);

        UserResponseDto response = new UserResponseDto(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRoles().stream()
                        .map(role -> role.getName())
                        .toList()
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping(ApiPaths.Users.CHANGE_PASSWORD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeMyPassword(
            @RequestBody ResetPasswordRequestDto request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        TokenResponseDto tokens =
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
