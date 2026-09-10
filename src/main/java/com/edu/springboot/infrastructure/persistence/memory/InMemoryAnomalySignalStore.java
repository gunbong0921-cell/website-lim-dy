package com.edu.springboot.infrastructure.persistence.memory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: InMemoryAnomalySignalStore
 * 책임: InMemoryAnomalySignalStore 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/technical/04-redis.md](../../../../../../../../../docs/technical/04-redis.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public class InMemoryAnomalySignalStore implements AnomalySignalStore {

	private final ConcurrentHashMap<String, Timed> values = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Deque<String>> lists = new ConcurrentHashMap<>();

	@Override
	public void rememberSignup(String loginId, long epochMilli, Duration ttl) {
		put("SIGNUP:" + loginId, Long.toString(epochMilli), ttl);
	}

	@Override
	public OptionalLong signupAt(String loginId) {
		return parseLong(get("SIGNUP:" + loginId));
	}

	@Override
	public boolean claimFirstCoreAction(String loginId, Duration ttl) {
		String key = "FIRST:" + loginId;
		Timed created = new Timed("1", Instant.now().plus(ttl));
		Timed previous = values.putIfAbsent(key, created);
		if (previous == null) {
			return true;
		}
		if (previous.expired()) {
			return values.replace(key, previous, created);
		}
		return false;
	}

	@Override
	public long incrementWindow(String loginId, String apiName, Duration window) {
		return increment("WIN:" + loginId + ":" + apiName, window);
	}

	@Override
	public void pushGet(String loginId, String route, Duration lookback, int maxEntries) {
		String key = "NAV:" + loginId;
		Deque<String> deque = lists.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		synchronized (deque) {
			deque.addFirst(System.currentTimeMillis() + ":" + route);
			while (deque.size() > Math.max(1, maxEntries)) {
				deque.removeLast();
			}
		}
		put(key, "1", lookback);
	}

	@Override
	public boolean hasGetSince(String loginId, long sinceEpochMilli) {
		Deque<String> deque = lists.get("NAV:" + loginId);
		if (deque == null) {
			return false;
		}
		synchronized (deque) {
			for (String entry : deque) {
				int colon = entry.indexOf(':');
				if (colon < 1) {
					continue;
				}
				try {
					if (Long.parseLong(entry.substring(0, colon)) >= sinceEpochMilli) {
						return true;
					}
				} catch (NumberFormatException ignored) {
					// skip
				}
			}
		}
		return false;
	}

	@Override
	public int incrementDirectCoreHits(String loginId, Duration ttl) {
		return (int) increment("DIRECT:" + loginId, ttl);
	}

	@Override
	public void clearDirectCoreHits(String loginId) {
		values.remove("DIRECT:" + loginId);
	}

	@Override
	public void markStep(String loginId, String step, Duration ttl) {
		put("STEP:" + loginId + ":" + step, "1", ttl);
	}

	@Override
	public boolean hasStep(String loginId, String step) {
		return get("STEP:" + loginId + ":" + step).isPresent();
	}

	private void put(String key, String value, Duration ttl) {
		values.put(key, new Timed(value, Instant.now().plus(ttl)));
	}

	private Optional<String> get(String key) {
		Timed timed = values.get(key);
		if (timed == null || timed.expired()) {
			values.remove(key);
			return Optional.empty();
		}
		return Optional.of(timed.value);
	}

	private OptionalLong parseLong(Optional<String> value) {
		if (value.isEmpty()) {
			return OptionalLong.empty();
		}
		try {
			return OptionalLong.of(Long.parseLong(value.get()));
		} catch (NumberFormatException ex) {
			return OptionalLong.empty();
		}
	}

	private long increment(String key, Duration ttl) {
		Timed next = values.compute(key, (ignored, current) -> {
			if (current == null || current.expired()) {
				return new Timed("1", Instant.now().plus(ttl));
			}
			long count = 1;
			try {
				count = Long.parseLong(current.value) + 1;
			} catch (NumberFormatException ignored2) {
				count = 1;
			}
			return new Timed(Long.toString(count), current.expires);
		});
		try {
			return Long.parseLong(next.value);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private record Timed(String value, Instant expires) {
		boolean expired() {
			return Instant.now().isAfter(expires);
		}
	}
}
