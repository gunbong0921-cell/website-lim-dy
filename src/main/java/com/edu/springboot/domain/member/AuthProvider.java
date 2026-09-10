package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: AuthProvider
 * 책임: AuthProvider 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum AuthProvider {
	LOCAL,
	GOOGLE,
	GITHUB,
	KAKAO;

	public boolean google() {
		return this == GOOGLE;
	}

	public boolean github() {
		return this == GITHUB;
	}

	public boolean kakao() {
		return this == KAKAO;
	}

	public boolean social() {
		return this == GOOGLE || this == GITHUB || this == KAKAO;
	}

	public String displayName() {
		return switch (this) {
			case GOOGLE -> "Google";
			case GITHUB -> "GitHub";
			case KAKAO -> "카카오";
			case LOCAL -> "이메일";
		};
	}

	public static AuthProvider from(String value) {
		if (value == null || value.isBlank()) {
			return LOCAL;
		}
		try {
			return AuthProvider.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return LOCAL;
		}
	}
}
