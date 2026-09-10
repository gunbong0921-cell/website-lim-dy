package com.edu.springboot.domain.security;

import java.time.Duration;
import java.util.Optional;

public interface RequestTicketStore {

	void save(String ticketId, String signingKey, Duration ttl);

	Optional<String> consume(String ticketId);
}
