package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: VerificationChannel
 * 책임: VerificationChannel 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum VerificationChannel {
	EMAIL,
	PHONE;

	public boolean phone() {
		return this == PHONE;
	}

	public static VerificationChannel from(String value) {
		if (value == null || value.isBlank()) {
			return EMAIL;
		}
		try {
			return VerificationChannel.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return EMAIL;
		}
	}
}
