package com.edu.springboot.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edu.springboot.domain.anomaly.AnomalyPolicy;
import com.edu.springboot.domain.board.LikePolicy;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.domain.captcha.CaptchaPolicy;
import com.edu.springboot.domain.file.ExtensionFileTypeClassifier;
import com.edu.springboot.domain.file.FileTypeClassifier;
import com.edu.springboot.domain.member.EmailPolicy;
import com.edu.springboot.domain.member.HoneypotPolicy;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.security.ClientAddressPolicy;
import com.edu.springboot.domain.security.RequestSignaturePolicy;

@Configuration
public class DomainBeanConfig {

	@Bean
	ViewCountPolicy viewCountPolicy() {
		return new ViewCountPolicy();
	}

	@Bean
	LikePolicy likePolicy() {
		return new LikePolicy();
	}

	@Bean
	PasswordPolicy passwordPolicy() {
		return new PasswordPolicy();
	}

	@Bean
	PhoneVerificationPolicy phoneVerificationPolicy() {
		return new PhoneVerificationPolicy();
	}

	@Bean
	CaptchaPolicy captchaPolicy() {
		return new CaptchaPolicy();
	}

	@Bean
	HoneypotPolicy honeypotPolicy() {
		return new HoneypotPolicy();
	}

	@Bean
	EmailPolicy emailPolicy() {
		return new EmailPolicy();
	}

	@Bean
	RequestSignaturePolicy requestSignaturePolicy() {
		return new RequestSignaturePolicy();
	}

	@Bean
	ClientAddressPolicy clientAddressPolicy(@Value("${app.security.trusted-proxy:false}") boolean trustedProxy) {
		return new ClientAddressPolicy(trustedProxy);
	}

	@Bean
	AnomalyPolicy anomalyPolicy() {
		return new AnomalyPolicy();
	}

	@Bean
	FileTypeClassifier fileTypeClassifier() {
		return new ExtensionFileTypeClassifier();
	}
}
