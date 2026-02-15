package com.fernandez.backend.service;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;

public interface IAuthService {
    void registerPublic(RegisterRequest request, String clientIp);
    AdminUserListResponse registerByAdmin(AdminCreateUserRequest request);
    TokenResponse login(LoginRequest request, String clientIp);
    TokenResponse refreshToken(String authHeader);
    void logout(String authHeader);
    TokenResponse resetPasswordFromProfile(String email, String newPassword);
    void updateFailedAttempts(String email);
    void unlockUser(User user);
}

