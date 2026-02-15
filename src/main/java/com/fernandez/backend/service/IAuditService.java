package com.fernandez.backend.service;

import com.fernandez.backend.dto.AuditLogDto;

import java.util.List;

public interface IAuditService {
    List<AuditLogDto> getGlobalAuditHistory();
}

