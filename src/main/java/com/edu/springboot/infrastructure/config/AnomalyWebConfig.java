package com.edu.springboot.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.edu.springboot.infrastructure.security.AnomalyNavigationInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: AnomalyWebConfig
 * 책임: 스프링 설정. 구현 빈을 포트에 꽂음
 * 문서: [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md)
 */
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
