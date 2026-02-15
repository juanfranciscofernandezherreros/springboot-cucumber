package com.fernandez.backend.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {
    /**
     * Secret key en Base64 (tal y como lo espera io.jsonwebtoken.Decoders.BASE64).
     */
    private String secretKey;

    /** Expiración del access token en ms. */
    private long expiration;

    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class RefreshToken {
        /** Expiración del refresh token en ms. */
        private long expiration;
    }
}

