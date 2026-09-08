package com.edu.springboot.presentation.controller;

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
}
