package com.edu.springboot.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.domain.file.ExtensionFileTypeClassifier;
import com.edu.springboot.domain.file.FileTypeClassifier;

@Configuration
public class DomainBeanConfig {

	@Bean
	ViewCountPolicy viewCountPolicy() {
		return new ViewCountPolicy();
	}

	@Bean
	FileTypeClassifier fileTypeClassifier() {
		return new ExtensionFileTypeClassifier();
	}
}
