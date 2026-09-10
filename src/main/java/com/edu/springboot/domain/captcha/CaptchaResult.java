package com.edu.springboot.domain.captcha;

public record CaptchaResult(boolean success, double score, String action) {

	public static CaptchaResult failed() {
		return new CaptchaResult(false, 0, "");
	}
}
