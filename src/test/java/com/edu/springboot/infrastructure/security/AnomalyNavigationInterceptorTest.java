package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.edu.springboot.domain.anomaly.AnomalyStep;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryAnomalySignalStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: AnomalyNavigationInterceptorTest
 * 책임: GET /me가 CONTEXT 단계를 남긴다. 판정 없음
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md)
 */
class AnomalyNavigationInterceptorTest {

	private final InMemoryAnomalySignalStore store = new InMemoryAnomalySignalStore();
	private final AnomalyNavigationInterceptor interceptor =
		new AnomalyNavigationInterceptor(store, true, 300, 24, 50);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("로그인 후 GET /me는 CONTEXT와 조회 기록을 남긴다")
	void preHandle_marksContextOnMe() {
		authenticate("user@gmail.com");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");

		assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
		assertThat(store.hasStep("user@gmail.com", AnomalyStep.CONTEXT)).isTrue();
		assertThat(store.hasGetSince("user@gmail.com", System.currentTimeMillis() - 1_000)).isTrue();
	}

	@Test
	@DisplayName("게시판 GET은 조회만 남기고 CONTEXT는 아니다")
	void preHandle_boardGetDoesNotMarkContext() {
		authenticate("user@gmail.com");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boards/qna");

		interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

		assertThat(store.hasGetSince("user@gmail.com", System.currentTimeMillis() - 1_000)).isTrue();
		assertThat(store.hasStep("user@gmail.com", AnomalyStep.CONTEXT)).isFalse();
	}

	@Test
	@DisplayName("비로그인이면 기록을 남기지 않는다")
	void preHandle_skipsAnonymous() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");

		interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

		assertThat(store.hasStep("user@gmail.com", AnomalyStep.CONTEXT)).isFalse();
	}

	private void authenticate(String loginId) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(loginId, "n", List.of())
		);
	}
}
