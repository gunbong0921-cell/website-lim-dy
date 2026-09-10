package com.edu.springboot.domain.captcha;

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
