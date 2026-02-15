package com.fernandez.backend.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwoFactorService {

    private static final String ISSUER = "SpringBoot-Security";
    private final DefaultSecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public TwoFactorService() {
        this.secretGenerator = new DefaultSecretGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    /**
     * Genera un nuevo secreto Base32
     * @return Secreto Base32 generado
     */
    public String generateSecretBase32() {
        return secretGenerator.generate();
    }

    /**
     * Genera la URL del código QR (otpauth://...)
     * @param email Email del usuario
     * @param secret Secreto Base32
     * @return URL en formato otpauth://
     */
    public String generateQrCodeUrl(String email, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
                ISSUER,
                email,
                secret,
                ISSUER,
                HashingAlgorithm.SHA1,
                6,
                30
        );
    }

    /**
     * Valida el código de 6 dígitos enviado por el usuario usando DefaultCodeVerifier
     * @param secret Secreto Base32 del usuario
     * @param code Código de 6 dígitos a validar
     * @return true si el código es válido, false en caso contrario
     */
    public boolean validateCode(String secret, String code) {
        if (secret == null || code == null) {
            log.warn("Intento de validación con secreto o código nulo");
            return false;
        }
        
        try {
            boolean isValid = codeVerifier.isValidCode(secret, code);
            if (isValid) {
                log.info("Código TOTP validado correctamente");
            } else {
                log.warn("Código TOTP inválido");
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error al validar código TOTP", e);
            return false;
        }
    }
}
