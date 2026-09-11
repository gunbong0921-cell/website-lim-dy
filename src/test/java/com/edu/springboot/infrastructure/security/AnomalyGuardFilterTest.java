package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.edu.springboot.domain.anomaly.AnomalyPolicy;
import com.edu.springboot.domain.anomaly.AnomalyStep;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.MemberRole;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryAnomalySignalStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: AnomalyGuardFilterTest
 * 책임: 핵심 쓰기의 시퀀스 401·TTFA 429·관리자 통과
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md)
 */
@ExtendWith(MockitoExtension.class)
class AnomalyGuardFilterTest {

	private static final String RATE_BODY =
		"{\"success\":false,\"data\":null,\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}";
	private static final String SESSION_BODY =
		"{\"success\":false,\"data\":null,\"message\":\"세션이 만료되었습니다. 다시 로그인해 주세요.\"}";

	@Mock
	private MemberRepository memberRepository;

	private InMemoryAnomalySignalStore store;
	private AnomalyGuardFilter filter;
	private Member member;

	@BeforeEach
	void setUp() {
		store = new InMemoryAnomalySignalStore();
		filter = new AnomalyGuardFilter(
			new AnomalyPolicy(), store, memberRepository,
			true, 3000, 24, 5, 60, 300, 24, 1
		);
		member = new Member();
		member.setLoginId("user@gmail.com");
		member.setCreatedAt(LocalDateTime.now().minusHours(1));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("/me 없이 핵심 쓰면 401이고 세션을 지운다")
	void sequence_unauthorizedWithoutContext() throws Exception {
		authenticate();
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		MockHttpServletRequest request = writeRequest();
		request.getSession(true);

		MockHttpServletResponse response = filter(request);

		assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
		assertThat(response.getContentAsString()).isEqualTo(SESSION_BODY);
		assertThat(request.getSession(false)).isNull();
	}

	@Test
	@DisplayName("가입 직후 첫 핵심 쓰기는 429이고 SUSPICIOUS로 표시한다")
	void ttfa_marksSuspicious() throws Exception {
		authenticate();
		member.setCreatedAt(LocalDateTime.now());
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		store.markStep("user@gmail.com", AnomalyStep.CONTEXT, java.time.Duration.ofHours(24));
		store.pushGet("user@gmail.com", "/api/members/me", java.time.Duration.ofMinutes(5), 50);
		store.rememberSignup("user@gmail.com", System.currentTimeMillis(), java.time.Duration.ofHours(24));

		MockHttpServletResponse response = filter(writeRequest());

		assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(response.getHeader("Retry-After")).isEqualTo("60");
		assertThat(response.getContentAsString()).isEqualTo(RATE_BODY);
		assertThat(member.suspicious()).isTrue();
		verify(memberRepository).save(member);
	}

	@Test
	@DisplayName("이미 SUSPICIOUS면 핵심 쓰기를 429로 막는다")
	void suspicious_alwaysRateLimited() throws Exception {
		authenticate();
		member.markSuspicious();
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));

		MockHttpServletResponse response = filter(writeRequest());

		assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("관리자는 핵심 쓰기를 건너뛴다")
	void admin_skipped() throws Exception {
		authenticate();
		member.assignRole(MemberRole.ADMIN);
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = writeRequest();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isSameAs(request);
	}

	@Test
	@DisplayName("비회원 쓰기는 이상 탐지를 타지 않는다")
	void anonymous_passesThrough() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = writeRequest();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isSameAs(request);
		verify(memberRepository, never()).findByLoginId(any());
	}

	@Test
	@DisplayName("스위치가 꺼져 있으면 검증을 건너뛴다")
	void disabled_skipped() throws Exception {
		filter = new AnomalyGuardFilter(
			new AnomalyPolicy(), store, memberRepository,
			false, 3000, 24, 5, 60, 300, 24, 1
		);
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = writeRequest();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isSameAs(request);
	}

	@Test
	@DisplayName("조회 요청은 핵심 쓰기가 아니라서 건너뛴다")
	void get_skipped() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isSameAs(request);
		verify(memberRepository, never()).findByLoginId(any());
	}

	@Test
	@DisplayName("CONTEXT는 있으나 최근 GET이 없으면 Zero-Nav 429다")
	void zeroNav_rateLimited() throws Exception {
		authenticate();
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		store.markStep("user@gmail.com", AnomalyStep.CONTEXT, java.time.Duration.ofHours(24));

		MockHttpServletResponse response = filter(writeRequest());

		assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(response.getHeader("Retry-After")).isEqualTo("60");
		assertThat(response.getContentAsString()).isEqualTo(RATE_BODY);
		assertThat(member.suspicious()).isFalse();
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("CONTEXT와 최근 GET이 있고 가입이 오래되면 통과한다")
	void allow_whenContextAndRecentGet() throws Exception {
		authenticate();
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		store.markStep("user@gmail.com", AnomalyStep.CONTEXT, java.time.Duration.ofHours(24));
		store.pushGet("user@gmail.com", "/api/members/me", java.time.Duration.ofMinutes(5), 50);
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = writeRequest();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isSameAs(request);
		verify(memberRepository, never()).save(any());
	}

	private void authenticate() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("user@gmail.com", "n", List.of())
		);
	}

	private MockHttpServletRequest writeRequest() {
		return new MockHttpServletRequest("POST", "/api/boards/qna");
	}

	private MockHttpServletResponse filter(MockHttpServletRequest request) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());
		return response;
	}
}
