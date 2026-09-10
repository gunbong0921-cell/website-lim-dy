package com.edu.springboot.domain.member;

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
