package com.fernandez.backend.service;

import com.fernandez.backend.config.IpLockProperties;
import com.fernandez.backend.utils.constants.ServiceStrings;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public class IpLockService implements IIpLockService {

    private final StringRedisTemplate redisTemplate;
    private final IpLockProperties properties;
    private static final String IP_PREFIX = ServiceStrings.IpLock.IP_PREFIX;
    private static final String IP_BLOCKED_PREFIX = ServiceStrings.IpLock.IP_BLOCKED_PREFIX;

    @Autowired
    public IpLockService(@Autowired(required = false) StringRedisTemplate redisTemplate,
                         IpLockProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void checkConfig() {
        if (properties.isEnabled()) {
            log.info(ServiceStrings.IpLock.LOG_ENABLED,
                    properties.getMaxAttempts(), properties.getLockTimeMinutes());
        } else {
            log.warn(ServiceStrings.IpLock.LOG_DISABLED);
        }
    }

    public boolean isIpBlocked(String ip) {
        if (!properties.isEnabled() || redisTemplate == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(IP_BLOCKED_PREFIX + ip));
        } catch (Exception e) {
            log.error(ServiceStrings.IpLock.LOG_REDIS_ERROR_ISIP, e.getMessage());
            return false; // Fail open: si Redis falla, dejamos pasar
        }
    }

    public void registerFailedAttempt(String ip) {
        if (!properties.isEnabled() || redisTemplate == null) return;
        try {
            String key = IP_PREFIX + ip;
            Long attempts = redisTemplate.opsForValue().increment(key);
            int maxAttempts = properties.getMaxAttempts();
            long lockTime = properties.getLockTimeMinutes();
            log.info(ServiceStrings.IpLock.LOG_REDIS_ATTEMPT, ip, attempts, maxAttempts);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }
            if (attempts != null && attempts >= maxAttempts) {
                // Bloqueamos por el tiempo configurado en el YML
                redisTemplate.opsForValue().set(IP_BLOCKED_PREFIX + ip, "true", lockTime, TimeUnit.MINUTES);
                // Borramos el contador de intentos (ya no hace falta, está bloqueado)
                redisTemplate.delete(key);
                log.error(ServiceStrings.IpLock.LOG_REDIS_BLOCK, ip, lockTime);
            }
        } catch (Exception e) {
            log.error(ServiceStrings.IpLock.LOG_REDIS_CONNECTION_ERROR, e.getMessage());
        }
    }
}