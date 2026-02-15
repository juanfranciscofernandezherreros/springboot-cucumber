package com.fernandez.backend.web.controller;

import com.fernandez.backend.application.port.in.IAuthService;
import com.fernandez.backend.shared.constants.ApiPaths;
import com.fernandez.backend.shared.dto.LoginRequest;
import com.fernandez.backend.shared.dto.RegisterRequestDto;
import com.fernandez.backend.shared.dto.TokenResponseDto;
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

    private final IAuthService service;

    @PostMapping(ApiPaths.Auth.REGISTER)
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequestDto request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        service.registerPublic(request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(ApiPaths.Auth.LOGIN)
    public ResponseEntity<TokenResponseDto> authenticate(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(service.login(request, clientIp));
    }

    @PostMapping(ApiPaths.Auth.REFRESH_TOKEN)
    public ResponseEntity<TokenResponseDto> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        TokenResponseDto response = service.refreshToken(authHeader);
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
