package com.edu.springboot.application.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.captcha.CaptchaPolicy;
import com.edu.springboot.domain.captcha.CaptchaResult;
import com.edu.springboot.domain.captcha.CaptchaVerifier;

@Service
public class VerifyCaptchaService {

	private static final String FAIL_MESSAGE = "로봇 확인에 실패했습니다. 다시 시도해 주세요.";

	private final CaptchaVerifier captchaVerifier;
	private final CaptchaPolicy captchaPolicy;
	private final boolean enabled;
	private final double minScore;
	private final String siteKey;
	private final String secretKey;

	public VerifyCaptchaService(
		CaptchaVerifier captchaVerifier,
		CaptchaPolicy captchaPolicy,
		@Value("${app.recaptcha.enabled:true}") boolean enabled,
		@Value("${app.recaptcha.min-score:0.5}") double minScore,
		@Value("${app.recaptcha.site-key:}") String siteKey,
		@Value("${app.recaptcha.secret-key:}") String secretKey
	) {
		this.captchaVerifier = captchaVerifier;
		this.captchaPolicy = captchaPolicy;
		this.enabled = enabled;
		this.minScore = minScore;
		this.siteKey = siteKey == null ? "" : siteKey.trim();
		this.secretKey = secretKey == null ? "" : secretKey.trim();
	}

	public boolean active() {
		return enabled && !siteKey.isBlank() && !secretKey.isBlank();
	}

	public void require(String token, CaptchaAction action, String clientIp, String requestHost) {
		if (!captchaPolicy.requiredOnHost(requestHost)) {
			return;
		}
		if (!active()) {
			return;
		}
		if (token == null || token.isBlank() || action == null) {
			throw new BusinessException(FAIL_MESSAGE);
		}
		CaptchaResult result = captchaVerifier.verify(token.trim(), action.value(), clientIp);
		if (!captchaPolicy.passed(result, action.value(), minScore)) {
			throw new BusinessException(FAIL_MESSAGE);
		}
	}
}
