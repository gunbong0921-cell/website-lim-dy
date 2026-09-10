package com.edu.springboot.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.edu.springboot.infrastructure.security.AnomalyNavigationInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AnomalyWebConfig implements WebMvcConfigurer {

	private final AnomalyNavigationInterceptor anomalyNavigationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(anomalyNavigationInterceptor)
			.addPathPatterns("/api/members/me", "/api/boards/**");
	}
}
