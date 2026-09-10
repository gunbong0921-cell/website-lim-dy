package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: DisposableEmailCatalog
 * 책임: DisposableEmailCatalog 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface DisposableEmailCatalog {

	boolean blocked(String domain);
}
