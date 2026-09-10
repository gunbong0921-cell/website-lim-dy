package com.edu.springboot.infrastructure.persistence.redis;

import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: RedisAnomalySignalStore
 * 책임: ANOMALY:* 가입시각·윈도·GET 리스트·단계
 * 문서: [docs/technical/04-redis.md](../../../../../../../../../docs/technical/04-redis.md) · [docs/security/07-anomaly-guard.md](../../../../../../../../../docs/security/07-anomaly-guard.md)
 */
public class RedisAnomalySignalStore implements AnomalySignalStore {

	private static final String SIGNUP = "ANOMALY:SIGNUP:";
	private static final String FIRST = "ANOMALY:FIRST:";
	private static final String WINDOW = "ANOMALY:NEW_USER:";
	private static final String NAV = "ANOMALY:NAV:";
	private static final String DIRECT = "ANOMALY:DIRECT:";
	private static final String STEP = "ANOMALY:STEP:";

	private final StringRedisTemplate redis;

	public RedisAnomalySignalStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void rememberSignup(String loginId, long epochMilli, Duration ttl) {
		redis.opsForValue().set(SIGNUP + loginId, Long.toString(epochMilli), ttl);
	}

	@Override
	public OptionalLong signupAt(String loginId) {
		String value = redis.opsForValue().get(SIGNUP + loginId);
		if (value == null || value.isBlank()) {
			return OptionalLong.empty();
		}
		try {
			return OptionalLong.of(Long.parseLong(value));
		} catch (NumberFormatException ex) {
			return OptionalLong.empty();
		}
	}

	@Override
	public boolean claimFirstCoreAction(String loginId, Duration ttl) {
		Boolean first = redis.opsForValue().setIfAbsent(FIRST + loginId, "1", ttl);
		return Boolean.TRUE.equals(first);
	}

	@Override
	public long incrementWindow(String loginId, String apiName, Duration window) {
		String key = WINDOW + loginId + ":" + apiName;
		Long count = redis.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redis.expire(key, window);
		}
		return count == null ? 0 : count;
	}

	@Override
	public void pushGet(String loginId, String route, Duration lookback, int maxEntries) {
		String key = NAV + loginId;
		redis.opsForList().leftPush(key, System.currentTimeMillis() + ":" + route);
		redis.opsForList().trim(key, 0, Math.max(0, maxEntries - 1));
		redis.expire(key, lookback);
	}

	@Override
	public boolean hasGetSince(String loginId, long sinceEpochMilli) {
		List<String> entries = redis.opsForList().range(NAV + loginId, 0, 49);
		if (entries == null || entries.isEmpty()) {
			return false;
		}
		for (String entry : entries) {
			int colon = entry.indexOf(':');
			if (colon < 1) {
				continue;
			}
			try {
				long at = Long.parseLong(entry.substring(0, colon));
				if (at >= sinceEpochMilli) {
					return true;
				}
			} catch (NumberFormatException ignored) {
				// skip bad entries
			}
		}
		return false;
	}

	@Override
	public int incrementDirectCoreHits(String loginId, Duration ttl) {
		String key = DIRECT + loginId;
		Long count = redis.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redis.expire(key, ttl);
		}
		return count == null ? 0 : count.intValue();
	}

	@Override
	public void clearDirectCoreHits(String loginId) {
		redis.delete(DIRECT + loginId);
	}

	@Override
	public void markStep(String loginId, String step, Duration ttl) {
		redis.opsForValue().set(STEP + loginId + ":" + step, "1", ttl);
	}

	@Override
	public boolean hasStep(String loginId, String step) {
		String value = redis.opsForValue().get(STEP + loginId + ":" + step);
		return value != null && !value.isBlank();
	}
}
