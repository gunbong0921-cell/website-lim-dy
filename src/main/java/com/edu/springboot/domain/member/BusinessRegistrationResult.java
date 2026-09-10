package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: BusinessRegistrationResult
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public record BusinessRegistrationResult(
	boolean matched,
	boolean operating,
	String statusName,
	String taxType,
	String message
) {
}
