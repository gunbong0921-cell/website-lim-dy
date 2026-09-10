package com.edu.springboot.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: BcryptPasswordEncryptor
 * 책임: BcryptPasswordEncryptor 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@RequiredArgsConstructor
public class BcryptPasswordEncryptor implements PasswordEncryptor {

	private final PasswordEncoder passwordEncoder;

	@Override
	public String encode(String raw) {
		return passwordEncoder.encode(raw);
	}

	@Override
	public boolean matches(String raw, String encoded) {
		return passwordEncoder.matches(raw, encoded);
	}
}
