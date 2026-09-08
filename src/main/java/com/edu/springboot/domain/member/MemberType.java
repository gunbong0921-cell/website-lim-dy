package com.edu.springboot.domain.member;

public enum MemberType {
	INDIVIDUAL,
	CORPORATE;

	public boolean corporate() {
		return this == CORPORATE;
	}

	public static MemberType from(String value) {
		if (value == null || value.isBlank()) {
			return INDIVIDUAL;
		}
		try {
			return MemberType.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return INDIVIDUAL;
		}
	}
}
