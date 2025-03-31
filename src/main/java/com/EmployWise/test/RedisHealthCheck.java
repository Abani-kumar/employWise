package com.EmployWise.test;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthCheck {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisHealthCheck(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void testRedisConnection() {
        try {
            stringRedisTemplate.opsForValue().set("testKey", "testValue");
            String value = stringRedisTemplate.opsForValue().get("testKey");
            System.out.println("✅ Redis Connected Successfully! Retrieved Value: " + value);
        } catch (Exception e) {
            System.err.println("❌ Redis Connection Failed: " + e.getMessage());
        }
    }
}

