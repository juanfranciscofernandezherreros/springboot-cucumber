package com.fernandez.backend.service;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;

public interface IAuthService {
    void registerPublic(RegisterRequestDto request, String clientIp);
    AdminUserListResponseDto registerByAdmin(AdminCreateUserRequestDto request);
    TokenResponseDto login(LoginRequest request, String clientIp);
    TokenResponseDto refreshToken(String authHeader);
    void logout(String authHeader);
    TokenResponseDto resetPasswordFromProfile(String email, String newPassword);
    void updateFailedAttempts(String email);
    void unlockUser(User user);
}
