package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.infrastructure.config.properties.CorsDbProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsPropsProvider provider;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsDbProps corsProps = provider.getCors();

        registry.addMapping(corsProps.getMapping())
                .allowedOrigins(corsProps.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(corsProps.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(corsProps.getAllowedHeaders().toArray(String[]::new))
                .exposedHeaders(corsProps.getExposedHeaders().toArray(String[]::new))
                .allowCredentials(corsProps.isAllowCredentials());
    }
}
