package com.edu.springboot.infrastructure.persistence.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.member.VerificationStore;

public class RedisPhoneVerificationStore implements VerificationStore {

	private static final String CODE_PREFIX = "PHONE_VERIFY:";
	private static final String COOLDOWN_PREFIX = "PHONE_VERIFY_COOLDOWN:";
	private static final String TOKEN_PREFIX = "PHONE_TOKEN:";

	private final StringRedisTemplate redis;

	public RedisPhoneVerificationStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void saveCode(String key, String code, Duration ttl) {
		redis.opsForValue().set(CODE_PREFIX + key, code, ttl);
	}

	@Override
	public Optional<String> findCode(String key) {
		return Optional.ofNullable(redis.opsForValue().get(CODE_PREFIX + key));
	}

	@Override
	public void deleteCode(String key) {
		redis.delete(CODE_PREFIX + key);
	}

	@Override
	public long cooldownRemainingSeconds(String key) {
		Long ttl = redis.getExpire(COOLDOWN_PREFIX + key, TimeUnit.SECONDS);
		return ttl == null || ttl < 0 ? 0 : ttl;
	}

	@Override
	public void startCooldown(String key, Duration ttl) {
		redis.opsForValue().set(COOLDOWN_PREFIX + key, "1", ttl);
	}

	@Override
	public long dailyCount(String bucketKey) {
		String value = redis.opsForValue().get(bucketKey);
		if (value == null || value.isBlank()) {
			return 0;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	@Override
	public long incrementDaily(String bucketKey, Duration ttl) {
		Long count = redis.opsForValue().increment(bucketKey);
		if (count != null && count == 1L) {
			redis.expire(bucketKey, ttl);
		}
		return count == null ? 0 : count;
	}

	@Override
	public void saveToken(String token, String targetKey, Duration ttl) {
		redis.opsForValue().set(TOKEN_PREFIX + token, targetKey, ttl);
	}

	@Override
	public Optional<String> consumeToken(String token) {
		String key = TOKEN_PREFIX + token;
		String phone = redis.opsForValue().getAndDelete(key);
		return Optional.ofNullable(phone);
	}
}
