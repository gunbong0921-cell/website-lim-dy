package com.edu.springboot.presentation.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.edu.springboot.domain.security.ClientAddressPolicy;

/**
 * Hexaq
 * 계층: Test
 * 객체: RequestClientIpTest
 * 책임: 헤더만 꺼내 ClientAddressPolicy에 전달
 * 문서: [docs/security/02-rate-limiting.md](../../../../../../../../docs/security/02-rate-limiting.md)
 */
class RequestClientIpTest {

	@Test
	@DisplayName("프록시를 믿지 않으면 remoteAddr만 넘긴다")
	void resolve_usesRemoteAddrWhenUntrusted() {
		RequestClientIp resolver = new RequestClientIp(new ClientAddressPolicy(false));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("1.1.1.1");
		request.addHeader("X-Forwarded-For", "9.9.9.9");
		request.addHeader("X-Real-IP", "8.8.8.8");

		assertThat(resolver.resolve(request)).isEqualTo("1.1.1.1");
	}

	@Test
	@DisplayName("신뢰 프록시면 X-Forwarded-For를 정책에 넘긴다")
	void resolve_usesForwardedForWhenTrusted() {
		RequestClientIp resolver = new RequestClientIp(new ClientAddressPolicy(true));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("10.0.0.1");
		request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

		assertThat(resolver.resolve(request)).isEqualTo("203.0.113.10");
	}
}
