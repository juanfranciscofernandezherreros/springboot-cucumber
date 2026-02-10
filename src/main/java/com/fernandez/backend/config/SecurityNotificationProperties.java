package com.fernandez.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityNotificationProperties {

    private boolean sendDataTelegramEnabled;
    private boolean sendDataEmailEnabled;
}
