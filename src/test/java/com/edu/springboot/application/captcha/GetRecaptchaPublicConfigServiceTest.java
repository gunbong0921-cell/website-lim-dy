package com.edu.springboot.application.captcha;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.domain.captcha.CaptchaPolicy;
import com.edu.springboot.domain.captcha.CaptchaVerifier;

/**
 * Hexaq
 * 계층: Test
 * 객체: GetRecaptchaPublicConfigServiceTest
 * 책임: 사이트 키만 공개. 터널·비활성이면 끔
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
@ExtendWith(MockitoExtension.class)
class GetRecaptchaPublicConfigServiceTest {

	@Mock
	private CaptchaVerifier captchaVerifier;

	@Test
	@DisplayName("활성이고 localhost면 사이트 키만 내린다")
	void get_returnsSiteKeyWithoutSecret() {
		VerifyCaptchaService verify = new VerifyCaptchaService(
			captchaVerifier, new CaptchaPolicy(), true, 0.5, "public-site-key", "server-secret");
		GetRecaptchaPublicConfigService service =
			new GetRecaptchaPublicConfigService(verify, new CaptchaPolicy(), "public-site-key");

		RecaptchaPublicConfig config = service.get("localhost:8282");

		assertThat(config.recaptchaEnabled()).isTrue();
		assertThat(config.recaptchaSiteKey()).isEqualTo("public-site-key");
		assertThat(config.toString()).doesNotContain("server-secret");
	}

	@Test
	@DisplayName("터널 호스트면 캡차를 끈다")
	void get_disablesOnTrycloudflare() {
		VerifyCaptchaService verify = new VerifyCaptchaService(
			captchaVerifier, new CaptchaPolicy(), true, 0.5, "public-site-key", "server-secret");
		GetRecaptchaPublicConfigService service =
			new GetRecaptchaPublicConfigService(verify, new CaptchaPolicy(), "public-site-key");

		RecaptchaPublicConfig config = service.get("abc.trycloudflare.com");

		assertThat(config).isEqualTo(new RecaptchaPublicConfig(false, ""));
	}

	@Test
	@DisplayName("키가 비면 캡차를 끈다")
	void get_disablesWhenInactive() {
		VerifyCaptchaService verify = new VerifyCaptchaService(
			captchaVerifier, new CaptchaPolicy(), true, 0.5, "", "server-secret");
		GetRecaptchaPublicConfigService service =
			new GetRecaptchaPublicConfigService(verify, new CaptchaPolicy(), "public-site-key");

		RecaptchaPublicConfig config = service.get("localhost");

		assertThat(config).isEqualTo(new RecaptchaPublicConfig(false, ""));
	}
}
