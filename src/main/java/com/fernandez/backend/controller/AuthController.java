package com.fernandez.backend.controller;

import com.fernandez.backend.dto.LoginRequest;
import com.fernandez.backend.dto.RegisterRequest;
import com.fernandez.backend.dto.TokenResponse;
import com.fernandez.backend.service.AuthService;
import com.fernandez.backend.utils.constants.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping(ApiPaths.Auth.REGISTER)
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        service.registerPublic(request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(ApiPaths.Auth.LOGIN)
    public ResponseEntity<TokenResponse> authenticate(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(service.login(request, clientIp));
    }

    @PostMapping(ApiPaths.Auth.REFRESH_TOKEN)
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        TokenResponse response = service.refreshToken(authHeader);
        return response == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(response);
    }

    @PostMapping(ApiPaths.Auth.LOGOUT)
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        service.logout(authHeader);
        return ResponseEntity.ok().build();
    }
}
