package com.fernandez.backend.service.impl;

import com.fernandez.backend.config.IpLockProperties;
import com.fernandez.backend.service.IIpLockService;
import com.fernandez.backend.utils.constants.ServiceStrings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class IpLockService implements IIpLockService {

    /**
     * Se mantiene para compatibilidad con la configuración actual (bean construye con redisTemplate),
     * pero en esta implementación usamos memoria (ConcurrentHashMap). Si en el futuro quieres Redis real,
     * aquí es donde se conectaría.
     */
    private final StringRedisTemplate redisTemplate;

    private final IpLockProperties properties;

    /** Intentos fallidos por IP en ventana de tiempo. */
    private final Map<String, Long> attemptsCache = new ConcurrentHashMap<>();

    /** Momento en el que se bloqueó la IP. */
    private final Map<String, Instant> blockCache = new ConcurrentHashMap<>();

    @Override
    public boolean isIpBlocked(String ip) {
        if (!properties.isEnabled()) {
            return false;
        }

        try {
            Instant blockTime = blockCache.get(ip);
            if (blockTime == null) {
                return false;
            }

            Duration elapsed = Duration.between(blockTime, Instant.now());
            if (elapsed.toMinutes() >= properties.getLockTimeMinutes()) {
                unblockIp(ip);
                return false;
            }

            return true;
        } catch (Exception e) {
            // Fail-open: si hay un error, no bloqueamos.
            log.error(ServiceStrings.IpLock.LOG_REDIS_ERROR_ISIP, e.getMessage());
            return false;
        }
    }

    @Override
    public void registerFailedAttempt(String ip) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            long attempts = attemptsCache.getOrDefault(ip, 0L);
            int maxAttempts = properties.getMaxAttempts();
            long lockTime = properties.getLockTimeMinutes();

            log.info(ServiceStrings.IpLock.LOG_REDIS_ATTEMPT, ip, attempts, maxAttempts);

            attempts++;

            if (attempts >= maxAttempts) {
                blockCache.put(ip, Instant.now());
                attemptsCache.remove(ip);
                log.error(ServiceStrings.IpLock.LOG_REDIS_BLOCK, ip, lockTime);
            } else {
                attemptsCache.put(ip, attempts);
            }
        } catch (Exception e) {
            log.error(ServiceStrings.IpLock.LOG_REDIS_CONNECTION_ERROR, e.getMessage());
        }
    }

    private void unblockIp(String ip) {
        blockCache.remove(ip);
        attemptsCache.remove(ip);
    }
}
