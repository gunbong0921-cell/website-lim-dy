package com.edu.springboot.domain.member;

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
