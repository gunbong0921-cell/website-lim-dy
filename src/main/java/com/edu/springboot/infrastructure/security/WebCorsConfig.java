package com.edu.springboot.infrastructure.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: WebCorsConfig
 * 책임: allowedOriginPatterns. https://*.trycloudflare.com 포함
 * 문서: [docs/technical/03-cloudflare-tunnel.md](../../../../../../../../docs/technical/03-cloudflare-tunnel.md)
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

	private static final String TUNNEL_ORIGIN_PATTERN = "https://*.trycloudflare.com";

	private final String[] allowedOrigins;

	public WebCorsConfig(@Value("${app.cors.allowed-origins:http://localhost:8282}") String allowedOrigins) {
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.toArray(String[]::new);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns(originPatterns().toArray(String[]::new))
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("Authorization", "Content-Type", "X-Hexaq-Timestamp", "X-Hexaq-Ticket",
				"X-Hexaq-Signature")
			.allowCredentials(true)
			.maxAge(3600);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(originPatterns());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Hexaq-Timestamp", "X-Hexaq-Ticket",
			"X-Hexaq-Signature"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	private List<String> originPatterns() {
		List<String> patterns = new ArrayList<>(Arrays.asList(allowedOrigins));
		if (patterns.stream().noneMatch(pattern -> pattern.contains("trycloudflare"))) {
			patterns.add(TUNNEL_ORIGIN_PATTERN);
		}
		return patterns;
	}
}
