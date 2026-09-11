package com.edu.springboot.domain.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: ClientAddressPolicyTest
 * 책임: 신뢰 프록시 여부에 따른 IP·호스트 규칙
 * 문서: [docs/security/02-rate-limiting.md](../../../../../../../../docs/security/02-rate-limiting.md)
 */
class ClientAddressPolicyTest {

	@Test
	@DisplayName("프록시를 믿지 않으면 포워드 헤더를 무시한다")
	void resolve_ignoresForwardedHeadersWhenUntrusted() {
		ClientAddressPolicy policy = new ClientAddressPolicy(false);

		assertThat(policy.resolve("1.1.1.1", "9.9.9.9, 10.0.0.1", "8.8.8.8")).isEqualTo("1.1.1.1");
	}

	@Test
	@DisplayName("프록시를 믿지 않고 remoteAddr가 비면 unknown이다")
	void resolve_unknownWhenRemoteBlank() {
		ClientAddressPolicy policy = new ClientAddressPolicy(false);

		assertThat(policy.resolve("  ", "9.9.9.9", "8.8.8.8")).isEqualTo("unknown");
		assertThat(policy.resolve(null, "9.9.9.9", null)).isEqualTo("unknown");
	}

	@Test
	@DisplayName("신뢰 프록시면 X-Forwarded-For 첫 주소를 쓴다")
	void resolve_usesFirstForwardedForWhenTrusted() {
		ClientAddressPolicy policy = new ClientAddressPolicy(true);

		assertThat(policy.resolve("10.0.0.1", " 203.0.113.10, 10.0.0.1 ", "8.8.8.8"))
			.isEqualTo("203.0.113.10");
	}

	@Test
	@DisplayName("신뢰 프록시이고 Forwarded-For가 없으면 X-Real-IP를 쓴다")
	void resolve_usesRealIpWhenForwardedMissing() {
		ClientAddressPolicy policy = new ClientAddressPolicy(true);

		assertThat(policy.resolve("10.0.0.1", "  ", " 203.0.113.20 ")).isEqualTo("203.0.113.20");
	}

	@Test
	@DisplayName("신뢰 프록시여도 헤더가 없으면 remoteAddr를 쓴다")
	void resolve_fallsBackToRemoteAddr() {
		ClientAddressPolicy policy = new ClientAddressPolicy(true);

		assertThat(policy.resolve(" 10.0.0.1 ", null, null)).isEqualTo("10.0.0.1");
	}

	@Test
	@DisplayName("신뢰 프록시면 X-Forwarded-Host 첫 값을 쓴다")
	void resolveHost_usesFirstForwardedHostWhenTrusted() {
		ClientAddressPolicy policy = new ClientAddressPolicy(true);

		assertThat(policy.resolveHost("localhost", " hexaq.example, localhost ")).isEqualTo("hexaq.example");
	}

	@Test
	@DisplayName("프록시를 믿지 않으면 서버 이름을 쓴다")
	void resolveHost_usesServerNameWhenUntrusted() {
		ClientAddressPolicy policy = new ClientAddressPolicy(false);

		assertThat(policy.resolveHost(" localhost ", "hexaq.example")).isEqualTo("localhost");
		assertThat(policy.resolveHost("  ", "hexaq.example")).isEqualTo("");
	}
}
