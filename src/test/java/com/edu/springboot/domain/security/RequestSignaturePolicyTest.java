package com.edu.springboot.domain.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: RequestSignaturePolicyTest
 * 책임: 시간 창·상수시간 서명 비교
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md)
 */
class RequestSignaturePolicyTest {

	private final RequestSignaturePolicy policy = new RequestSignaturePolicy();

	@Test
	@DisplayName("타임스탬프가 허용 오차 안이면 통과한다")
	void timestampInWindow_acceptsWithinSkew() {
		assertThat(policy.timestampInWindow(1_000L, 1_000L, 300_000L)).isTrue();
		assertThat(policy.timestampInWindow(1_000L, 301_000L, 300_000L)).isTrue();
		assertThat(policy.timestampInWindow(301_000L, 1_000L, 300_000L)).isTrue();
	}

	@Test
	@DisplayName("타임스탬프가 허용 오차를 넘으면 거절한다")
	void timestampInWindow_rejectsOutsideSkew() {
		assertThat(policy.timestampInWindow(1_000L, 301_001L, 300_000L)).isFalse();
	}

	@Test
	@DisplayName("허용 오차가 음수면 거절한다")
	void timestampInWindow_rejectsNegativeSkew() {
		assertThat(policy.timestampInWindow(1_000L, 1_000L, -1L)).isFalse();
	}

	@Test
	@DisplayName("서명은 대소문자를 무시하고 같으면 통과한다")
	void signaturesMatch_ignoresHexCase() {
		assertThat(policy.signaturesMatch("abcDEF", "ABCDEF")).isTrue();
		assertThat(policy.signaturesMatch("abcdef", "abcdee")).isFalse();
	}

	@Test
	@DisplayName("서명이 null이거나 길이가 다르면 거절한다")
	void signaturesMatch_rejectsNullOrDifferentLength() {
		assertThat(policy.signaturesMatch(null, "ab")).isFalse();
		assertThat(policy.signaturesMatch("ab", null)).isFalse();
		assertThat(policy.signaturesMatch("ab", "abcd")).isFalse();
	}
}
