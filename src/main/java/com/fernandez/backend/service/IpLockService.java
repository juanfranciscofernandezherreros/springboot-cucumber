package com.fernandez.backend.service;

import com.fernandez.backend.config.IpLockProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IpLockService {

    private final StringRedisTemplate redisTemplate;
    private final IpLockProperties properties;
    private static final String IP_PREFIX = "IP_ATTEMPT:";
    private static final String IP_BLOCKED_PREFIX = "IP_BLOCKED:";

    @Autowired
    public IpLockService(@Autowired(required = false) StringRedisTemplate redisTemplate,
                         IpLockProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void checkConfig() {
        if (properties.isEnabled()) {
            log.info("🛡️ SEGURIDAD: Bloqueo IP ACTIVADO. Máx Intentos: {}, Tiempo Bloqueo: {} min",
                    properties.getMaxAttempts(), properties.getLockTimeMinutes());
        } else {
            log.warn("⚠️ SEGURIDAD: El sistema de bloqueo por IP (Redis) está DESACTIVADO desde config.");
        }
    }

    public boolean isIpBlocked(String ip) {
        if (!properties.isEnabled() || redisTemplate == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(IP_BLOCKED_PREFIX + ip));
        } catch (Exception e) {
            log.error("Error al consultar Redis (isIpBlocked): {}", e.getMessage());
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
            log.info("🔍 REDIS: Intento fallido registrado para IP: {}. Intentos actuales: {}/{}", ip, attempts, maxAttempts);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }
            if (attempts != null && attempts >= maxAttempts) {
                // Bloqueamos por el tiempo configurado en el YML
                redisTemplate.opsForValue().set(IP_BLOCKED_PREFIX + ip, "true", lockTime, TimeUnit.MINUTES);
                // Borramos el contador de intentos (ya no hace falta, está bloqueado)
                redisTemplate.delete(key);
                log.error("🚫 REDIS: ¡IP BLOQUEADA! -> {} por {} minutos", ip, lockTime);
            }
        } catch (Exception e) {
            log.error("❌ REDIS: Error al conectar con el servidor Redis: {}", e.getMessage());
        }
    }
}