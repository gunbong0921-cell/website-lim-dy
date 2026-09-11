package com.edu.springboot.domain.captcha;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: CaptchaActionTest
 * 책임: 공개 쓰기 7개 action 문자열
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
class CaptchaActionTest {

	@Test
	@DisplayName("문서의 7개 action과 같다")
	void values_matchDocumentedActions() {
		assertThat(Arrays.stream(CaptchaAction.values()).map(CaptchaAction::value))
			.containsExactly(
				"login",
				"signup",
				"email_send_code",
				"phone_send_code",
				"forgot_id",
				"forgot_password",
				"board_write_free"
			);
	}

	@Test
	@DisplayName("네트워크 실패 결과는 success=false다")
	void failed_isUnsuccessfulZeroScore() {
		assertThat(CaptchaResult.failed()).isEqualTo(new CaptchaResult(false, 0, ""));
	}
}
