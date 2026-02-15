package com.fernandez.backend.application.port.in;

import com.fernandez.backend.shared.dto.*;
import com.fernandez.backend.domain.model.User;

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
