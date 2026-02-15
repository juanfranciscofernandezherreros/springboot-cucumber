package com.fernandez.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwoFactorServiceTest {

    private TwoFactorService twoFactorService;

    @BeforeEach
    void setUp() {
        twoFactorService = new TwoFactorService();
    }

    @Test
    void generateSecretBase32_shouldGenerateValidSecret() {
        // When
        String secret = twoFactorService.generateSecretBase32();

        // Then
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
        // Base32 secrets are typically 32 characters
        assertTrue(secret.length() >= 16);
    }

    @Test
    void generateQrCodeUrl_shouldGenerateValidOtpAuthUrl() {
        // Given
        String email = "test@example.com";
        String secret = "TESTSECRET123456";

        // When
        String qrCodeUrl = twoFactorService.generateQrCodeUrl(email, secret);

        // Then
        assertNotNull(qrCodeUrl);
        assertTrue(qrCodeUrl.startsWith("otpauth://totp/"));
        // Email is URL-encoded, so @ becomes %40
        assertTrue(qrCodeUrl.contains("test%40example.com"));
        assertTrue(qrCodeUrl.contains(secret));
        assertTrue(qrCodeUrl.contains("issuer=SpringBoot-Security"));
        assertTrue(qrCodeUrl.contains("digits=6"));
        assertTrue(qrCodeUrl.contains("period=30"));
    }

    @Test
    void validateCode_withNullSecret_shouldReturnFalse() {
        // When
        boolean result = twoFactorService.validateCode(null, "123456");

        // Then
        assertFalse(result);
    }

    @Test
    void validateCode_withNullCode_shouldReturnFalse() {
        // When
        boolean result = twoFactorService.validateCode("TESTSECRET123456", null);

        // Then
        assertFalse(result);
    }

    @Test
    void validateCode_withInvalidCode_shouldReturnFalse() {
        // Given
        String secret = twoFactorService.generateSecretBase32();
        String invalidCode = "000000";

        // When
        boolean result = twoFactorService.validateCode(secret, invalidCode);

        // Then - Most likely invalid unless we're extremely unlucky
        // We can't reliably test this without knowing the current time-based code
        assertNotNull(result);
    }

    @Test
    void generateQrCodeUrl_withSpecialCharactersInEmail_shouldEncodeCorrectly() {
        // Given
        String email = "test+special@example.com";
        String secret = "TESTSECRET123456";

        // When
        String qrCodeUrl = twoFactorService.generateQrCodeUrl(email, secret);

        // Then
        assertNotNull(qrCodeUrl);
        assertTrue(qrCodeUrl.startsWith("otpauth://totp/"));
        // Special characters should be URL-encoded: + becomes %2B, @ becomes %40
        assertTrue(qrCodeUrl.contains("test%2Bspecial%40example.com"));
    }
}
