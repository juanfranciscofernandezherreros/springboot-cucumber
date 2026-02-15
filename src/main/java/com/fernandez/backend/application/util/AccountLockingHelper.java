package com.fernandez.backend.application.util;

import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * Helper class for managing user account locking and unlocking operations.
 * 
 * <p>Centralizes logic for:
 * <ul>
 *   <li>Processing failed login attempts</li>
 *   <li>Locking accounts after max failed attempts</li>
 *   <li>Unlocking accounts</li>
 *   <li>Resetting failed attempt counters</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AccountLockingHelper {

    private final UserRepository userRepository;

    /**
     * Processes a failed login attempt for a user.
     * 
     * <p>Increments the failed attempt counter and locks the account if the
     * maximum number of failed attempts has been reached.
     *
     * @param user the user who failed to authenticate
     * @param maxFailedAttempts the maximum number of allowed failed attempts before locking
     */
    public void processFailedAttempt(User user, int maxFailedAttempts) {
        int failedAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(failedAttempts);

        if (failedAttempts >= maxFailedAttempts) {
            user.setAccountNonLocked(false);
            user.setLockCount(user.getLockCount() + 1);
            user.setLockTime(new Date());
        }

        userRepository.save(user);
    }

    /**
     * Resets all failed attempt counters and unlocks the user account.
     *
     * @param user the user to reset
     */
    public void resetAllAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockCount(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    /**
     * Unlocks a user account and resets the failed attempt counter.
     *
     * @param user the user to unlock
     */
    public void unlockUser(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    /**
     * Checks if a user account should be unlocked based on the lock duration.
     *
     * @param user the user to check
     * @param durationMillis the lock duration in milliseconds
     * @return true if the account should be unlocked, false otherwise
     */
    public boolean shouldUnlock(User user, long durationMillis) {
        if (user.getLockTime() == null) {
            return true;
        }
        return user.getLockTime().getTime() + durationMillis < System.currentTimeMillis();
    }
}
