package com.edu.springboot.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: HoneypotPolicyTest
 * 책임: null/blank만 통과. 값이 있으면 tripped
 * 문서: [docs/security/04-honeypot-field.md](../../../../../../../../docs/security/04-honeypot-field.md)
 */
class HoneypotPolicyTest {

	private final HoneypotPolicy policy = new HoneypotPolicy();

	@Test
	@DisplayName("null과 공백은 통과한다")
	void tripped_allowsNullAndBlank() {
		assertThat(policy.tripped(null)).isFalse();
		assertThat(policy.tripped("")).isFalse();
		assertThat(policy.tripped("  ")).isFalse();
	}

	@Test
	@DisplayName("값이 있으면 봇으로 본다")
	void tripped_rejectsFilledWebsite() {
		assertThat(policy.tripped("https://spam.example")).isTrue();
		assertThat(policy.tripped(" http://bot ")).isTrue();
	}
}
