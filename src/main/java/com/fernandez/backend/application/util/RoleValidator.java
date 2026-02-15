package com.fernandez.backend.application.util;

import com.fernandez.backend.domain.model.Role;
import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.shared.constants.AuthServiceConstants;

import java.util.Collection;

/**
 * Utility class for validating user roles.
 * 
 * <p>Provides methods to check if a user has specific roles or privileges.
 * Uses centralized role constants to avoid hardcoded strings.
 */
public final class RoleValidator {

    private RoleValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a user has the ADMIN role.
     *
     * @param user the user to check
     * @return true if user has ADMIN role, false otherwise
     */
    public static boolean isAdmin(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> AuthServiceConstants.ROLE_ADMIN.equals(role.getName()));
    }

    /**
     * Checks if a user has a specific role by name.
     *
     * @param user the user to check
     * @param roleName the name of the role to check for
     * @return true if user has the specified role, false otherwise
     */
    public static boolean hasRole(User user, String roleName) {
        if (user == null || user.getRoles() == null || roleName == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    /**
     * Checks if a user has any of the specified roles.
     *
     * @param user the user to check
     * @param roleNames the collection of role names to check for
     * @return true if user has at least one of the specified roles, false otherwise
     */
    public static boolean hasAnyRole(User user, Collection<String> roleNames) {
        if (user == null || user.getRoles() == null || roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleNames::contains);
    }

    /**
     * Checks if a user has all of the specified roles.
     *
     * @param user the user to check
     * @param roleNames the collection of role names to check for
     * @return true if user has all of the specified roles, false otherwise
     */
    public static boolean hasAllRoles(User user, Collection<String> roleNames) {
        if (user == null || user.getRoles() == null || roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        var userRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        return userRoleNames.containsAll(roleNames);
    }
}
