package com.edu.springboot.application.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edu.springboot.domain.captcha.CaptchaPolicy;

/**
 * Hexaq
 * 계층: Application
 * 객체: GetRecaptchaPublicConfigService
 * 책임: 프론트용 사이트 키. 비밀 키 없음. 터널 호스트면 끔
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
@Service
public class GetRecaptchaPublicConfigService {

	private final VerifyCaptchaService verifyCaptchaService;
	private final CaptchaPolicy captchaPolicy;
	private final String siteKey;

	public GetRecaptchaPublicConfigService(
		VerifyCaptchaService verifyCaptchaService,
		CaptchaPolicy captchaPolicy,
		@Value("${app.recaptcha.site-key:}") String siteKey
	) {
		this.verifyCaptchaService = verifyCaptchaService;
		this.captchaPolicy = captchaPolicy;
		this.siteKey = siteKey == null ? "" : siteKey.trim();
	}

	public RecaptchaPublicConfig get(String requestHost) {
		if (!captchaPolicy.requiredOnHost(requestHost) || !verifyCaptchaService.active()) {
			return new RecaptchaPublicConfig(false, "");
		}
		return new RecaptchaPublicConfig(true, siteKey);
	}
}
