package com.fernandez.backend.shared.dto;

import lombok.Builder;

@Builder
public record AuditLogDto(
        String revInfo,
        String timestamp,
        String author,
        String operation,
        String entityName,
        String description,
        Object snapshot
) {}
