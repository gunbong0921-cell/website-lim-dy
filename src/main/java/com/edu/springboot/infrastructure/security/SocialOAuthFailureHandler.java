package com.edu.springboot.infrastructure.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SocialOAuthFailureHandler implements AuthenticationFailureHandler {

	@Value("${app.oauth.failure-redirect:/#/login}")
	private String failureRedirect;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {
		String encoded = URLEncoder.encode("소셜 로그인에 실패했습니다.", StandardCharsets.UTF_8);
		response.sendRedirect(failureRedirect + "?oauthError=" + encoded);
	}
}
