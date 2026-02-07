package com.fernandez.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogDto {
    private String revInfo;      // REV-894
    private String timestamp;    // 2025-03-02 15:10
    private String author;       // admin
    private String operation;    // CREATE, UPDATE, DELETE
    private String entityName;   // Usuario, Invitación
    private String description;  // "ID #45" o el email
    private Object snapshot;     // Los datos reales
}
