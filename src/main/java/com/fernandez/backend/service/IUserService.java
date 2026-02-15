package com.fernandez.backend.service;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.model.User;

import java.util.List;

public interface IUserService {
    UserStatsResponse getUserStatistics();
    List<AdminUserListResponse> getAllUsers();
    List<AdminUserListResponse> getLockedUsers();
    void unlockUser(String email);
    void lockUser(String email);
    void deleteUserById(Long id);
    void updateUserRole(String email, String roleName);
    AdminUserListResponse updateUserByAdmin(Long id, AdminUpdateUserRequest request);
    AdminUserListResponse getUserStatus(String email);
    User updateMyProfile(String email, UpdateUserRequest request);
}

