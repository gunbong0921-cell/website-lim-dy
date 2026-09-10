package com.edu.springboot.domain.member;

import java.util.regex.Pattern;

/**
 * Hexaq
 * 계층: Domain
 * 객체: PasswordPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class PasswordPolicy {

	private static final int MIN_LENGTH = 8;
	private static final int MAX_LENGTH = 20;
	private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
	private static final Pattern DIGIT = Pattern.compile("[0-9]");
	private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");
	private static final String RULE_MESSAGE = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.";

	public boolean matches(String password) {
		if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
			return false;
		}
		if (password.chars().anyMatch(Character::isWhitespace)) {
			return false;
		}
		return LETTER.matcher(password).find()
			&& DIGIT.matcher(password).find()
			&& SPECIAL.matcher(password).find();
	}

	public String ruleMessage() {
		return RULE_MESSAGE;
	}
}
