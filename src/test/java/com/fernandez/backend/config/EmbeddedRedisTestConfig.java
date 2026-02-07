package com.fernandez.backend.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisTestConfig {

    @Value("${application.security.redis-enabled}")
    private boolean redisEnabled;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {

        if (!redisEnabled) {
            return;
        }

        redisServer = RedisServer.builder()
                .port(63799)
                .setting("maxmemory 128M")
                .build();

        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
