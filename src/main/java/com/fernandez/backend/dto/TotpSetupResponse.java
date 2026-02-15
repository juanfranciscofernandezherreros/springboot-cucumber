package com.fernandez.backend.dto;

public record TotpSetupResponse(
        String secret,
        String qrCodeUri
) {
}
