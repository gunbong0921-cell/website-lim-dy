package com.edu.springboot.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edu.springboot.domain.board.LikePolicy;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.domain.file.ExtensionFileTypeClassifier;
import com.edu.springboot.domain.file.FileTypeClassifier;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;

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
	FileTypeClassifier fileTypeClassifier() {
		return new ExtensionFileTypeClassifier();
	}
}
