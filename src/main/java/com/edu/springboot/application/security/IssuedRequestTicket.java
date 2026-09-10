package com.edu.springboot.application.security;

import java.time.Instant;

/**
 * Hexaq
 * 계층: Application
 * 객체: IssuedRequestTicket
 * 책임: IssuedRequestTicket 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public record IssuedRequestTicket(String ticketId, String signingKey, Instant expiresAt) {
}
