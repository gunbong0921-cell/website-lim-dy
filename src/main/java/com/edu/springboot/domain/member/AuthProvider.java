package com.edu.springboot.domain.member;

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
