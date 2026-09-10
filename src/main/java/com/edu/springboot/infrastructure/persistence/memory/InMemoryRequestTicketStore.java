package com.edu.springboot.infrastructure.persistence.memory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.edu.springboot.domain.security.RequestTicketStore;

public class InMemoryRequestTicketStore implements RequestTicketStore {

	private final ConcurrentHashMap<String, Entry> values = new ConcurrentHashMap<>();

	@Override
	public void save(String ticketId, String signingKey, Duration ttl) {
		values.put(ticketId, new Entry(signingKey, Instant.now().plus(ttl)));
	}

	@Override
	public Optional<String> consume(String ticketId) {
		if (ticketId == null || ticketId.isBlank()) {
			return Optional.empty();
		}
		Entry entry = values.remove(ticketId.trim());
		if (entry == null || entry.expired()) {
			return Optional.empty();
		}
		return Optional.of(entry.value);
	}

	private record Entry(String value, Instant expires) {
		boolean expired() {
			return Instant.now().isAfter(expires);
		}
	}
}
