package com.edu.springboot.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final SocialOAuthSuccessHandler socialOAuthSuccessHandler;
	private final SocialOAuthFailureHandler socialOAuthFailureHandler;

	@Bean
	org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
		return username -> {
			throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.headers(headers -> headers
				.contentTypeOptions(Customizer.withDefaults())
				.frameOptions(frame -> frame.deny())
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/members/me", "/api/members/profile", "/api/members/password").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/boards/qna", "/api/boards/archive", "/api/comments/**",
					"/api/boards/qna/*/like", "/api/boards/archive/*/like").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/boards/qna/**", "/api/boards/archive/**", "/api/comments/**")
					.authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/boards/qna/**", "/api/boards/archive/**", "/api/comments/**")
					.authenticated()
				.anyRequest().permitAll()
			)
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.oauth2Login(oauth -> oauth
				.successHandler(socialOAuthSuccessHandler)
				.failureHandler(socialOAuthFailureHandler)
			)
			.logout(logout -> logout.disable())
			.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"로그인이 필요합니다.\"}");
			}));
		return http.build();
	}
}
