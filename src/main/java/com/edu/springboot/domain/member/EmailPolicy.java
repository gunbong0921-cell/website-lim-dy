package com.edu.springboot.domain.member;

import java.util.regex.Pattern;

/**
 * Hexaq
 * 계층: Domain
 * 객체: EmailPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class EmailPolicy {

	private static final int MAX_LENGTH = 100;
	private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public boolean validFormat(String email) {
		if (email == null || email.isBlank() || email.length() > MAX_LENGTH) {
			return false;
		}
		if (email.contains("..") || email.startsWith(".") || email.contains("@.") || email.contains(".@")) {
			return false;
		}
		return EMAIL.matcher(email).matches();
	}

	public String domainOf(String email) {
		if (email == null) {
			return "";
		}
		int at = email.lastIndexOf('@');
		if (at < 0 || at == email.length() - 1) {
			return "";
		}
		return email.substring(at + 1).toLowerCase();
	}
}
