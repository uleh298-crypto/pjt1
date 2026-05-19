package com.ssafy.ssabre.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class NotificationTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "fcm:token:";
    private static final long TOKEN_TTL_DAYS = 60; // 2 months

    public void saveToken(Long memberId, String token) {
        redisTemplate.opsForValue().set(TOKEN_PREFIX + memberId, token, Duration.ofDays(TOKEN_TTL_DAYS));
    }

    public String getToken(Long memberId) {
        return redisTemplate.opsForValue().get(TOKEN_PREFIX + memberId);
    }

    public void deleteToken(Long memberId) {
        redisTemplate.delete(TOKEN_PREFIX + memberId);
    }
}
