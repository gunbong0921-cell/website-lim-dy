package com.edu.springboot.domain.member;

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
