package com.edu.springboot.presentation.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

final class AuthSupport {

	private AuthSupport() {
	}

	static String loginId(Authentication authentication) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return authentication.getName();
	}
}
