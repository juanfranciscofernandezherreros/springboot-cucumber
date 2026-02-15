package com.fernandez.backend.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.lock")
public class SecurityLockProperties {
    private Map<Integer, Long> durations;
}
