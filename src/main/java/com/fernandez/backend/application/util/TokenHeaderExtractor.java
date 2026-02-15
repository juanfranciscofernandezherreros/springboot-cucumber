package com.fernandez.backend.application.util;

import com.fernandez.backend.shared.constants.AuthServiceConstants;

/**
 * Utility class for extracting and validating JWT tokens from HTTP Authorization headers.
 * 
 * <p>This class provides methods to:
 * <ul>
 *   <li>Validate Authorization header format</li>
 *   <li>Extract Bearer tokens from headers</li>
 *   <li>Check if header contains a valid Bearer token</li>
 * </ul>
 */
public final class TokenHeaderExtractor {

    private TokenHeaderExtractor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if the Authorization header is valid and starts with "Bearer ".
     *
     * @param authHeader the Authorization header value
     * @return true if header is not null and starts with Bearer prefix, false otherwise
     */
    public static boolean isValidBearerHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(AuthServiceConstants.TOKEN_PREFIX);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * 
     * <p>Expects header format: "Bearer {token}"
     *
     * @param authHeader the Authorization header value
     * @return the extracted token, or null if header is invalid
     */
    public static String extractToken(String authHeader) {
        if (!isValidBearerHeader(authHeader)) {
            return null;
        }
        return authHeader.substring(AuthServiceConstants.TOKEN_PREFIX.length());
    }

    /**
     * Extracts the JWT token from the Authorization header with exception on invalid header.
     *
     * @param authHeader the Authorization header value
     * @return the extracted token
     * @throws IllegalArgumentException if header is null or doesn't start with Bearer prefix
     */
    public static String extractTokenOrThrow(String authHeader) {
        if (!isValidBearerHeader(authHeader)) {
            throw new IllegalArgumentException("Invalid Authorization header: must start with 'Bearer '");
        }
        return authHeader.substring(AuthServiceConstants.TOKEN_PREFIX.length());
    }
}
