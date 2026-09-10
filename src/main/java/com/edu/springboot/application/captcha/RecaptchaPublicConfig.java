package com.edu.springboot.application.captcha;

/**
 * Hexaq
 * 계층: Application
 * 객체: RecaptchaPublicConfig
 * 책임: 스프링 설정. 구현 빈을 포트에 꽂음
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public record RecaptchaPublicConfig(boolean recaptchaEnabled, String recaptchaSiteKey) {
}
