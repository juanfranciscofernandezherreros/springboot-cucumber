package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.application.util.AppPropertyResolver;
import com.fernandez.backend.infrastructure.config.properties.CorsDbProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CorsPropsProvider {

    private final AppPropertyResolver resolver;

    public CorsDbProps getCors() {
        CorsDbProps props = new CorsDbProps();

        props.setMapping(resolver.getRaw("app.cors.mapping").orElse("/**"));
        props.setAllowedOrigins(resolver.getCsvList("app.cors.allowed-origins", List.of("http://localhost:3000")));
        props.setAllowedMethods(resolver.getCsvList("app.cors.allowed-methods", List.of("GET", "POST")));
        props.setAllowedHeaders(resolver.getCsvList("app.cors.allowed-headers", List.of("*")));
        props.setExposedHeaders(resolver.getCsvList("app.cors.exposed-headers", List.of("Authorization")));
        props.setAllowCredentials(resolver.getBoolean("app.cors.allow-credentials", true));

        return props;
    }
}

