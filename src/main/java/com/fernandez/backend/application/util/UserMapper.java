package com.fernandez.backend.application.util;

import com.fernandez.backend.domain.model.Role;
import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.shared.dto.AdminUserListResponseDto;

/**
 * Utility class for mapping between User domain entities and DTOs.
 * 
 * <p>Centralizes User-to-DTO conversion logic to avoid duplication across services.
 */
public final class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Maps a User entity to an AdminUserListResponseDto.
     * 
     * <p>This method extracts user information including roles, lock status,
     * failed attempts, and lock count for administrative purposes.
     *
     * @param user the User entity to map
     * @return the AdminUserListResponseDto with user information
     * @throws NullPointerException if user is null
     */
    public static AdminUserListResponseDto toAdminUserListResponse(User user) {
        if (user == null) {
            throw new NullPointerException("User cannot be null");
        }
        
        return new AdminUserListResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.isAccountNonLocked(),
                user.getFailedAttempt(),
                user.getLockCount()
        );
    }
}
