package io.fundy.fundyserver.register.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // 기존: millis 기반
    public void setAuthCode(String email, String code, long expirationMillis) {
        redisTemplate.opsForValue().set(email, code, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // Duration 오버로딩 메서드 추가 (이게 핵심!)
    public void setValues(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean checkExistsValue(String value) {
        return value != null && !value.isEmpty();
    }

    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public void deleteAuthCode(String email) {
        redisTemplate.delete(email);
    }

    // Duration 기반 get/set/delete 오버로딩이 필요하다면 추가로 구현 가능
}
