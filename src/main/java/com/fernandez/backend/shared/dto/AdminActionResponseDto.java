package com.fernandez.backend.shared.dto;

import lombok.Builder;

@Builder
public record AdminActionResponseDto<T>(
        String mensaje,
        T data
) {}

