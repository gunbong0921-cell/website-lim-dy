package com.edu.springboot.presentation.controller;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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
