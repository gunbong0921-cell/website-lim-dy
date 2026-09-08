package com.edu.springboot.application.member.dto;

public record SignUpResult(Long memberId, boolean mailSent, String debugCode, String verificationChannel) {

	public boolean needsEmailVerification() {
		return false;
	}
}
