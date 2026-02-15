package com.fernandez.backend.infrastructure.config.properties;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CorsDbProps {
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
    private boolean allowCredentials = true;
    private String mapping = "/**";
}

