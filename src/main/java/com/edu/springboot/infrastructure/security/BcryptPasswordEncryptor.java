package com.edu.springboot.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

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
