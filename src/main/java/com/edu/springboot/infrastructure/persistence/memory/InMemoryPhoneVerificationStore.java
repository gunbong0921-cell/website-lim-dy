package com.edu.springboot.infrastructure.persistence.memory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.edu.springboot.domain.member.PhoneVerificationStore;

public class InMemoryPhoneVerificationStore implements PhoneVerificationStore {

	private static final String CODE_PREFIX = "PHONE_VERIFY:";
	private static final String COOLDOWN_PREFIX = "PHONE_VERIFY_COOLDOWN:";
	private static final String TOKEN_PREFIX = "PHONE_TOKEN:";

	private final ConcurrentHashMap<String, Entry> values = new ConcurrentHashMap<>();

	@Override
	public void saveCode(String phone, String code, Duration ttl) {
		put(CODE_PREFIX + phone, code, ttl);
	}

	@Override
	public Optional<String> findCode(String phone) {
		return get(CODE_PREFIX + phone);
	}

	@Override
	public void deleteCode(String phone) {
		values.remove(CODE_PREFIX + phone);
	}

	@Override
	public long cooldownRemainingSeconds(String phone) {
		Entry entry = values.get(COOLDOWN_PREFIX + phone);
		if (entry == null || entry.expired()) {
			values.remove(COOLDOWN_PREFIX + phone);
			return 0;
		}
		return Math.max(0, Duration.between(Instant.now(), entry.expires).toSeconds());
	}

	@Override
	public void startCooldown(String phone, Duration ttl) {
		put(COOLDOWN_PREFIX + phone, "1", ttl);
	}

	@Override
	public long dailyCount(String bucketKey) {
		return get(bucketKey).map(value -> {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ex) {
				return 0L;
			}
		}).orElse(0L);
	}

	@Override
	public long incrementDaily(String bucketKey, Duration ttl) {
		Entry current = values.get(bucketKey);
		long next = 1;
		if (current != null && !current.expired()) {
			try {
				next = Long.parseLong(current.value) + 1;
			} catch (NumberFormatException ignored) {
				next = 1;
			}
			put(bucketKey, Long.toString(next), Duration.between(Instant.now(), current.expires));
			return next;
		}
		put(bucketKey, "1", ttl);
		return 1;
	}

	@Override
	public void saveToken(String token, String phone, Duration ttl) {
		put(TOKEN_PREFIX + token, phone, ttl);
	}

	@Override
	public Optional<String> consumeToken(String token) {
		Entry removed = values.remove(TOKEN_PREFIX + token);
		if (removed == null || removed.expired()) {
			return Optional.empty();
		}
		return Optional.of(removed.value);
	}

	private void put(String key, String value, Duration ttl) {
		Duration safe = ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
		values.put(key, new Entry(value, Instant.now().plus(safe)));
	}

	private Optional<String> get(String key) {
		Entry entry = values.get(key);
		if (entry == null || entry.expired()) {
			values.remove(key);
			return Optional.empty();
		}
		return Optional.of(entry.value);
	}

	private record Entry(String value, Instant expires) {
		boolean expired() {
			return !Instant.now().isBefore(expires);
		}
	}
}
