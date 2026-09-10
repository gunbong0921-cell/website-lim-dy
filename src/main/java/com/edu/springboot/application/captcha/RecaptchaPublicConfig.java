package com.edu.springboot.application.captcha;

public record RecaptchaPublicConfig(boolean recaptchaEnabled, String recaptchaSiteKey) {
}
