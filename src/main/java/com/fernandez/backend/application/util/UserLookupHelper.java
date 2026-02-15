package com.fernandez.backend.application.util;

import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.UserRepository;
import com.fernandez.backend.shared.constants.ServiceStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper class for looking up users from the repository with consistent error handling.
 * 
 * <p>Centralizes user lookup logic to avoid duplication across services and ensure
 * consistent exception messages.
 */
@Component
@RequiredArgsConstructor
public class UserLookupHelper {

    private final UserRepository userRepository;

    /**
     * Finds a user by email address.
     *
     * @param email the email address to search for
     * @return the User entity
     * @throws RuntimeException if user is not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_USER_NOT_FOUND_PREFIX + email));
    }

    /**
     * Finds a user by ID.
     *
     * @param id the user ID to search for
     * @return the User entity
     * @throws RuntimeException if user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ServiceStrings.User.ERR_USER_NOT_FOUND_ID_PREFIX + id));
    }
}
