package com.fernandez.backend.service.impl;

}
    }
        blockCache.remove(ip);
    private void unblockIp(String ip) {

    }
        }
            log.error(ServiceStrings.IpLock.LOG_REDIS_CONNECTION_ERROR, e.getMessage());
        } catch (Exception e) {
            }
                attemptsCache.put(ip, attempts + 1);
            } else {
                log.error(ServiceStrings.IpLock.LOG_REDIS_BLOCK, ip, lockTime);
                attemptsCache.remove(ip);
                blockCache.put(ip, Instant.now());
            } else if (attempts >= maxAttempts) {
                attemptsCache.put(ip, 1L);
            if (attempts == null) {

            log.info(ServiceStrings.IpLock.LOG_REDIS_ATTEMPT, ip, attempts, maxAttempts);

            long lockTime = properties.getLockTimeMinutes();
            int maxAttempts = properties.getMaxAttempts();
            Long attempts = attemptsCache.get(ip);
        try {
        if (!properties.isEnabled()) return;
    public void registerFailedAttempt(String ip) {
    @Override

    }
        }
            return false;
            log.error(ServiceStrings.IpLock.LOG_REDIS_ERROR_ISIP, e.getMessage());
        } catch (Exception e) {
            return false;
            }
                return true;
                }
                    return false;
                    unblockIp(ip);
                if (minutesBlocked >= properties.getLockTimeMinutes()) {
                long minutesBlocked = Instant.now().minusMillis(blockTime.toEpochMilli()).toEpochMilli() / 60000;
            if (blockTime != null) {
            Instant blockTime = blockCache.get(ip);
        try {
        }
            return false;
        if (!properties.isEnabled()) {
    public boolean isIpBlocked(String ip) {
    @Override

    private final Map<String, Instant> blockCache = new ConcurrentHashMap<>();
    private final Map<String, Long> attemptsCache = new ConcurrentHashMap<>();

    private final IpLockProperties properties;
    private final StringRedisTemplate redisTemplate; // mantenido para compat de firma

public class IpLockService implements IIpLockService {
@Slf4j
@RequiredArgsConstructor
@Service

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.fernandez.backend.utils.constants.ServiceStrings;
import com.fernandez.backend.service.IIpLockService;
import com.fernandez.backend.config.IpLockProperties;

