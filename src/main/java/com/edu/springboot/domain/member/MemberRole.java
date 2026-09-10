package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: MemberRole
 * 책임: MemberRole 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum MemberRole {
	USER,
	ADMIN;

	public boolean isAdmin() {
		return this == ADMIN;
	}

	public static MemberRole from(String value) {
		if (value == null || value.isBlank()) {
			return USER;
		}
		try {
			return MemberRole.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return USER;
		}
	}
}
