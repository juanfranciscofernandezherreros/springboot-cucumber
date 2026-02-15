package com.fernandez.backend.controller;

import com.fernandez.backend.dto.*;
import com.fernandez.backend.service.AuthService;
import com.fernandez.backend.utils.constants.AuthApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AuthApiPaths.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping(AuthApiPaths.REGISTER)
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        service.registerPublic(request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(AuthApiPaths.LOGIN)
    public ResponseEntity<TokenResponse> authenticate(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(service.login(request, clientIp));
    }

    @PostMapping(AuthApiPaths.REFRESH_TOKEN)
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        TokenResponse response = service.refreshToken(authHeader);
        return response == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(response);
    }

    @PostMapping(AuthApiPaths.LOGOUT)
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        service.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        // Extraer el email del contexto de seguridad
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(service.setupTotp(email));
    }

    @PostMapping("/totp/enable")
    public ResponseEntity<Void> enableTotp(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody TotpVerifyRequest request
    ) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        service.enableTotp(email, request.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/totp/disable")
    public ResponseEntity<Void> disableTotp(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        service.disableTotp(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/totp/status")
    public ResponseEntity<TotpStatusResponse> getTotpStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(service.getTotpStatus(email));
    }

    @PostMapping("/totp/login")
    public ResponseEntity<TokenResponse> loginWithTotp(
            @RequestBody LoginWithTotpRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(service.loginWithTotp(request, clientIp));
    }
}
