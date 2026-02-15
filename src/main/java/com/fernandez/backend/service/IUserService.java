package com.fernandez.backend.service;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;

import java.util.List;

public interface IUserService {
    UserStatsResponseDto getUserStatistics();
    List<AdminUserListResponseDto> getAllUsers();
    List<AdminUserListResponseDto> getLockedUsers();
    void unlockUser(String email);
    void lockUser(String email);
    void deleteUserById(Long id);
    void updateUserRole(String email, String roleName);
    AdminUserListResponseDto updateUserByAdmin(Long id, AdminUpdateUserRequestDto request);
    AdminUserListResponseDto getUserStatus(String email);
    User updateMyProfile(String email, UpdateUserRequestDto request);
}
