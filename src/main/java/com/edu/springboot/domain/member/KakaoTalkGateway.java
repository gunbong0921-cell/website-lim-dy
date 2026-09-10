package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: KakaoTalkGateway
 * 책임: 외부 시스템 포트 또는 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface KakaoTalkGateway {

	boolean sendWelcome(String accessToken, String memberName);
}
