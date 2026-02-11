package com.fernandez.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminActionResponse<T> {
    private String mensaje;
    private T data;
}