package com.edu.springboot.domain.captcha;

public interface CaptchaVerifier {

	CaptchaResult verify(String token, String action, String remoteIp);
}
