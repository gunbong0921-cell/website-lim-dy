package com.edu.springboot.domain.member;

/**
 * Hexaq
 * 계층: Domain
 * 객체: HoneypotPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/security/04-honeypot-field.md](../../../../../../../../docs/security/04-honeypot-field.md)
 */
public class HoneypotPolicy {

	public boolean tripped(String website) {
		return website != null && !website.isBlank();
	}
}
