package com.fernandez.backend.web.controller;

import com.fernandez.backend.shared.dto.AuditLogDto;
import com.fernandez.backend.application.port.in.IAuditService;
import com.fernandez.backend.shared.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Audit.BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final IAuditService auditService;

    @GetMapping(ApiPaths.Audit.HISTORY)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<AuditLogDto>> getFullHistory() {
        return ResponseEntity.ok(auditService.getGlobalAuditHistory());
    }
}
