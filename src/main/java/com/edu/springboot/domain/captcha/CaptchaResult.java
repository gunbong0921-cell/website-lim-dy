package com.edu.springboot.domain.captcha;

/**
 * Hexaq
 * 계층: Domain
 * 객체: CaptchaResult
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public record CaptchaResult(boolean success, double score, String action) {

	public static CaptchaResult failed() {
		return new CaptchaResult(false, 0, "");
	}
}
