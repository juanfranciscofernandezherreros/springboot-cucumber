package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.infrastructure.config.properties.CorsProps;
import com.fernandez.backend.infrastructure.config.properties.JwtProperties;
import com.fernandez.backend.infrastructure.config.properties.TelegramProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        TelegramProperties.class,
        CorsProps.class
})
public class PropertiesConfig {
}

