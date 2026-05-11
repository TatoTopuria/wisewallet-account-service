package com.wisewallet.account.infrastructure.redis;

import com.wisewallet.account.application.port.out.TokenStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenStoreAdapter implements TokenStorePort {

    private static final String KEY_PREFIX = "jwt:blocklist:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void blockJti(String jti, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Failed to write JWT blocklist key jti={}: {}", jti, e.getMessage());
        }
    }

    @Override
    public boolean isBlocked(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            log.warn("Failed to check JWT blocklist key jti={}: {}", jti, e.getMessage());
            return false; // fail-open per gateway plan
        }
    }
}
