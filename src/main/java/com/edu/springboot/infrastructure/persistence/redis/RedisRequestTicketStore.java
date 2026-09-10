package com.edu.springboot.infrastructure.persistence.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.security.RequestTicketStore;

public class RedisRequestTicketStore implements RequestTicketStore {

	private static final String PREFIX = "REQ_TICKET:";

	private final StringRedisTemplate redis;

	public RedisRequestTicketStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void save(String ticketId, String signingKey, Duration ttl) {
		redis.opsForValue().set(PREFIX + ticketId, signingKey, ttl);
	}

	@Override
	public Optional<String> consume(String ticketId) {
		if (ticketId == null || ticketId.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(redis.opsForValue().getAndDelete(PREFIX + ticketId.trim()));
	}
}
