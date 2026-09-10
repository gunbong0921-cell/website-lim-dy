package com.edu.springboot.presentation.controller;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: CookieSupport
 * 책임: CookieSupport 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
final class CookieSupport {

	private CookieSupport() {
	}

	static boolean has(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return false;
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return true;
			}
		}
		return false;
	}

	static ResponseCookie viewedToday(String name, int maxAgeSeconds) {
		return ResponseCookie.from(name, "Y")
			.path("/")
			.maxAge(maxAgeSeconds)
			.sameSite("Lax")
			.build();
	}
}
