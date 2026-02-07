package com.fernandez.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.security.ip-lock")
public class IpLockProperties {
    private boolean enabled = true;
    private int maxAttempts = 3;
    private long lockTimeMinutes = 5;
}