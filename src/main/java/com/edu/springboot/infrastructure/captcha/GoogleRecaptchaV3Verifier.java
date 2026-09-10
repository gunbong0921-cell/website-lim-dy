package com.edu.springboot.infrastructure.captcha;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.edu.springboot.domain.captcha.CaptchaResult;
import com.edu.springboot.domain.captcha.CaptchaVerifier;

@Component
public class GoogleRecaptchaV3Verifier implements CaptchaVerifier {

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private final RestClient restClient;
	private final String verifyUrl;
	private final String secretKey;

	public GoogleRecaptchaV3Verifier(
		@Value("${app.recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}") String verifyUrl,
		@Value("${app.recaptcha.secret-key:}") String secretKey
	) {
		this.restClient = RestClient.builder().build();
		this.verifyUrl = verifyUrl;
		this.secretKey = secretKey == null ? "" : secretKey.trim();
	}

	@Override
	public CaptchaResult verify(String token, String action, String remoteIp) {
		if (secretKey.isBlank() || token == null || token.isBlank()) {
			return CaptchaResult.failed();
		}
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("secret", secretKey);
			form.add("response", token);
			if (remoteIp != null && !remoteIp.isBlank()) {
				form.add("remoteip", remoteIp);
			}
			Map<String, Object> body = restClient.post()
				.uri(URI.create(verifyUrl))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(MAP_TYPE);
			if (body == null) {
				return CaptchaResult.failed();
			}
			boolean success = Boolean.TRUE.equals(body.get("success"));
			double score = toScore(body.get("score"));
			String returnedAction = body.get("action") == null ? "" : String.valueOf(body.get("action"));
			return new CaptchaResult(success, score, returnedAction);
		} catch (Exception ex) {
			return CaptchaResult.failed();
		}
	}

	private double toScore(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		if (value == null) {
			return 0;
		}
		try {
			return Double.parseDouble(String.valueOf(value));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
}
