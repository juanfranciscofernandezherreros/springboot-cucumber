package com.fernandez.backend.application.port.in;

import com.fernandez.backend.shared.dto.AuditLogDto;

import java.util.List;

public interface IAuditService {
    List<AuditLogDto> getGlobalAuditHistory();
}
