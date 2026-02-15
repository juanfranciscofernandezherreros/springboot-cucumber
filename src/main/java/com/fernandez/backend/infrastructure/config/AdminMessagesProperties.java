package com.fernandez.backend.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "app.admin.messages")
public class AdminMessagesProperties {
    private String userLocked;
    private String userUnlocked;
    private String roleUpdated;
    private String userDeleted;
    private String userCreated;
}
