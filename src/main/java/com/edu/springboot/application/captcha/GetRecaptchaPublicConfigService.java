package com.edu.springboot.application.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edu.springboot.domain.captcha.CaptchaPolicy;

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
