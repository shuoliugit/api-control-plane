package com.example.apicontrolplane.sandbox;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
  private final StringRedisTemplate redis;
  private final int limit;
  private final ConcurrentHashMap<String, AtomicInteger> fallbackCounters = new ConcurrentHashMap<>();

  public RateLimiter(StringRedisTemplate redis, @Value("${app.sandbox.rate-limit-per-minute}") int limit) {
    this.redis = redis;
    this.limit = limit;
  }

  public boolean allow(String clientId) {
    String key = "rate:sandbox:" + clientId + ":" + java.time.Instant.now().getEpochSecond() / 60;
    try {
      Long count = redis.opsForValue().increment(key);
      if (count != null && count == 1L) {
        redis.expire(key, Duration.ofMinutes(2));
      }
      return count == null || count <= limit;
    } catch (RedisConnectionFailureException ex) {
      return fallbackCounters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet() <= limit;
    }
  }
}
