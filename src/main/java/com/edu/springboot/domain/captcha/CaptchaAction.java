package com.edu.springboot.domain.captcha;

/**
 * Hexaq
 * 계층: Domain
 * 객체: CaptchaAction
 * 책임: CaptchaAction 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum CaptchaAction {

	LOGIN("login"),
	SIGNUP("signup"),
	EMAIL_SEND_CODE("email_send_code"),
	PHONE_SEND_CODE("phone_send_code"),
	FORGOT_ID("forgot_id"),
	FORGOT_PASSWORD("forgot_password"),
	BOARD_WRITE_FREE("board_write_free");

	private final String value;

	CaptchaAction(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
