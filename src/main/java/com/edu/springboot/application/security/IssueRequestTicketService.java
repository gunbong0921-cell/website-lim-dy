package com.edu.springboot.application.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edu.springboot.domain.security.RequestTicket;
import com.edu.springboot.domain.security.RequestTicketStore;

/**
 * Hexaq
 * 계층: Application
 * 객체: IssueRequestTicketService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
public class IssueRequestTicketService {

	private final RequestTicketStore requestTicketStore;
	private final Duration ttl;
	private final SecureRandom random = new SecureRandom();

	public IssueRequestTicketService(
		RequestTicketStore requestTicketStore,
		@Value("${app.request-signing.ticket-ttl-seconds:60}") int ttlSeconds
	) {
		this.requestTicketStore = requestTicketStore;
		this.ttl = Duration.ofSeconds(Math.max(1, ttlSeconds));
	}

	public IssuedRequestTicket issue() {
		byte[] keyBytes = new byte[32];
		random.nextBytes(keyBytes);
		RequestTicket ticket = new RequestTicket(
			UUID.randomUUID().toString(),
			HexFormat.of().formatHex(keyBytes),
			Instant.now().plus(ttl)
		);
		requestTicketStore.save(ticket.ticketId(), ticket.signingKey(), ttl);
		return new IssuedRequestTicket(ticket.ticketId(), ticket.signingKey(), ticket.expiresAt());
	}
}
