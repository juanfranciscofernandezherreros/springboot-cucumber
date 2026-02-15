package com.fernandez.backend.dto;

import lombok.Builder;

@Builder
public record AdminActionResponseDto<T>(
        String mensaje,
        T data
) {}

