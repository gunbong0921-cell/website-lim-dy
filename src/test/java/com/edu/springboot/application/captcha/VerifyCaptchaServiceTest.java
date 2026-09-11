package com.edu.springboot.application.captcha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.captcha.CaptchaPolicy;
import com.edu.springboot.domain.captcha.CaptchaResult;
import com.edu.springboot.domain.captcha.CaptchaVerifier;

/**
 * Hexaq
 * 계층: Test
 * 객체: VerifyCaptchaServiceTest
 * 책임: 터널·비활성 no-op, 점수 실패 메시지
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
@ExtendWith(MockitoExtension.class)
class VerifyCaptchaServiceTest {

	private static final String FAIL = "로봇 확인에 실패했습니다. 다시 시도해 주세요.";

	@Mock
	private CaptchaVerifier captchaVerifier;

	private final CaptchaPolicy policy = new CaptchaPolicy();

	@Test
	@DisplayName("사이트 키와 비밀 키가 있을 때만 활성이다")
	void active_requiresEnabledAndBothKeys() {
		assertThat(service(true, "site", "secret").active()).isTrue();
		assertThat(service(false, "site", "secret").active()).isFalse();
		assertThat(service(true, "", "secret").active()).isFalse();
		assertThat(service(true, "site", "  ").active()).isFalse();
	}

	@Test
	@DisplayName("터널 호스트면 검증기를 부르지 않는다")
	void require_skipsTrycloudflareHost() {
		service(true, "site", "secret").require("token", CaptchaAction.LOGIN, "1.1.1.1", "abc.trycloudflare.com");

		verify(captchaVerifier, never()).verify(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("비활성이면 검증기를 부르지 않는다")
	void require_skipsWhenInactive() {
		service(true, "", "secret").require("token", CaptchaAction.PHONE_SEND_CODE, "1.1.1.1", "localhost");

		verify(captchaVerifier, never()).verify(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("토큰이 비면 실패 메시지로 거절한다")
	void require_rejectsBlankToken() {
		VerifyCaptchaService service = service(true, "site", "secret");

		assertThatThrownBy(() -> service.require("  ", CaptchaAction.LOGIN, "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage(FAIL);
		verify(captchaVerifier, never()).verify(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("점수가 컷오프 미만이면 거절한다")
	void require_rejectsLowScore() {
		when(captchaVerifier.verify("tok", "phone_send_code", "1.1.1.1"))
			.thenReturn(new CaptchaResult(true, 0.3, "phone_send_code"));
		VerifyCaptchaService service = service(true, "site", "secret");

		assertThatThrownBy(() -> service.require(" tok ", CaptchaAction.PHONE_SEND_CODE, "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage(FAIL);
	}

	@Test
	@DisplayName("action이 다르면 거절한다")
	void require_rejectsActionMismatch() {
		when(captchaVerifier.verify("tok", "login", "1.1.1.1"))
			.thenReturn(new CaptchaResult(true, 0.9, "signup"));
		VerifyCaptchaService service = service(true, "site", "secret");

		assertThatThrownBy(() -> service.require("tok", CaptchaAction.LOGIN, "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage(FAIL);
	}

	@Test
	@DisplayName("점수와 action이 맞으면 통과한다")
	void require_acceptsPassingScore() {
		when(captchaVerifier.verify("tok", "login", "1.1.1.1"))
			.thenReturn(new CaptchaResult(true, 0.9, "login"));

		service(true, "site", "secret").require("tok", CaptchaAction.LOGIN, "1.1.1.1", "localhost");

		verify(captchaVerifier).verify("tok", "login", "1.1.1.1");
	}

	private VerifyCaptchaService service(boolean enabled, String siteKey, String secretKey) {
		return new VerifyCaptchaService(captchaVerifier, policy, enabled, 0.5, siteKey, secretKey);
	}
}
