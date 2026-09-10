package com.edu.springboot.domain.captcha;

/**
 * Hexaq
 * 계층: Domain
 * 객체: CaptchaVerifier
 * 책임: CaptchaVerifier 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface CaptchaVerifier {

	CaptchaResult verify(String token, String action, String remoteIp);
}
