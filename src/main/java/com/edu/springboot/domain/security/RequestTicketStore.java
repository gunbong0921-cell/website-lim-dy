package com.edu.springboot.domain.security;

import java.time.Duration;
import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: RequestTicketStore
 * 책임: RequestTicketStore 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface RequestTicketStore {

	void save(String ticketId, String signingKey, Duration ttl);

	Optional<String> consume(String ticketId);
}
