package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: MemberTrustStatus
 * 책임: MemberTrustStatus 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum MemberTrustStatus {
	ACTIVE,
	SUSPICIOUS;

	public boolean suspicious() {
		return this == SUSPICIOUS;
	}

	public static MemberTrustStatus from(String value) {
		if (value == null || value.isBlank()) {
			return ACTIVE;
		}
		try {
			return MemberTrustStatus.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return ACTIVE;
		}
	}
}
