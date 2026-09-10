package com.edu.springboot.presentation.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: AuthSupport
 * 책임: AuthSupport 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
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
