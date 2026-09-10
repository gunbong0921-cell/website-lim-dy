package com.edu.springboot.infrastructure.security;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: SessionPrincipal
 * 책임: SessionPrincipal 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public record SessionPrincipal(String loginId, boolean admin) {
}
