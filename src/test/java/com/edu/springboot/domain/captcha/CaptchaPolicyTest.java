package com.edu.springboot.domain.captcha;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: CaptchaPolicyTest
 * 책임: 점수·action 판정과 터널 호스트 제외
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
class CaptchaPolicyTest {

	private final CaptchaPolicy policy = new CaptchaPolicy();

	@Test
	@DisplayName("성공이고 action이 같고 점수가 컷오프 이상이면 통과한다")
	void passed_acceptsMatchingActionAtCutoff() {
		CaptchaResult result = new CaptchaResult(true, 0.5, "login");

		assertThat(policy.passed(result, "login", 0.5)).isTrue();
		assertThat(policy.passed(result, "login", 0.51)).isFalse();
	}

	@Test
	@DisplayName("실패·null·action 불일치면 거절한다")
	void passed_rejectsFailureNullAndActionMismatch() {
		assertThat(policy.passed(null, "login", 0.5)).isFalse();
		assertThat(policy.passed(CaptchaResult.failed(), "login", 0.5)).isFalse();
		assertThat(policy.passed(new CaptchaResult(true, 0.9, "signup"), "login", 0.5)).isFalse();
		assertThat(policy.passed(new CaptchaResult(true, 0.9, "login"), "  ", 0.5)).isFalse();
	}

	@Test
	@DisplayName("localhost는 캡차가 필요하고 터널 호스트는 아니다")
	void requiredOnHost_skipsTrycloudflareOnly() {
		assertThat(policy.requiredOnHost("localhost:8282")).isTrue();
		assertThat(policy.requiredOnHost("")).isTrue();
		assertThat(policy.requiredOnHost("abc.trycloudflare.com")).isFalse();
		assertThat(policy.requiredOnHost("ABC.TryCloudflare.COM:443")).isFalse();
	}
}
