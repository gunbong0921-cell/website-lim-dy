package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.edu.springboot.domain.security.RequestSignaturePolicy;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryRequestTicketStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: RequestSignatureFilterTest
 * 책임: 공개 쓰기 7개 POST의 시간 창·HMAC·1회 소비
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md)
 */
class RequestSignatureFilterTest {

	private static final String FAIL_BODY = "{\"success\":false,\"data\":null,\"message\":\"요청이 유효하지 않습니다.\"}";
	private static final String LOGIN = "/api/auth/login";
	private static final String BODY = "{\"loginId\":\"user\"}";

	private InMemoryRequestTicketStore store;
	private RequestSignatureFilter filter;
	private MockFilterChain chain;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		store = new InMemoryRequestTicketStore();
		filter = new RequestSignatureFilter(new RequestSignaturePolicy(), store, true, 300);
		chain = new MockFilterChain();
		response = new MockHttpServletResponse();
	}

	@Test
	@DisplayName("올바른 서명이면 필터를 통과하고 바디를 재사용할 수 있다")
	void validSignature_continuesChainWithCachedBody() throws Exception {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));
		MockHttpServletRequest request = signed(LOGIN, BODY, "ticket-1", "signing-key", System.currentTimeMillis());

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(chain.getRequest()).isInstanceOf(CachedBodyHttpServletRequest.class);
		assertThat(((CachedBodyHttpServletRequest) chain.getRequest()).cachedBody())
			.isEqualTo(BODY.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("헤더가 없으면 400이다")
	void missingHeaders_rejected() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", LOGIN);
		request.setContent(BODY.getBytes(StandardCharsets.UTF_8));

		filter.doFilter(request, response, chain);

		assertRejected();
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	@DisplayName("타임스탬프가 숫자가 아니면 400이다")
	void invalidTimestamp_rejected() throws Exception {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));
		MockHttpServletRequest request = signed(LOGIN, BODY, "ticket-1", "signing-key", System.currentTimeMillis());
		request = withTimestamp(request, "not-a-number");

		filter.doFilter(request, response, chain);

		assertRejected();
	}

	@Test
	@DisplayName("시간 창을 벗어나면 티켓을 소비하지 않고 400이다")
	void timestampOutOfWindow_rejectedWithoutConsuming() throws Exception {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));
		long stale = System.currentTimeMillis() - 301_000L;
		MockHttpServletRequest request = signed(LOGIN, BODY, "ticket-1", "signing-key", stale);

		filter.doFilter(request, response, chain);

		assertRejected();
		assertThat(store.consume("ticket-1")).contains("signing-key");
	}

	@Test
	@DisplayName("없는 티켓이면 400이다")
	void unknownTicket_rejected() throws Exception {
		MockHttpServletRequest request = signed(LOGIN, BODY, "missing", "signing-key", System.currentTimeMillis());

		filter.doFilter(request, response, chain);

		assertRejected();
	}

	@Test
	@DisplayName("서명이 틀리면 400이고 티켓은 이미 소비된다")
	void wrongSignature_rejectedAndTicketConsumed() throws Exception {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));
		MockHttpServletRequest request = signed(LOGIN, BODY, "ticket-1", "other-key", System.currentTimeMillis());

		filter.doFilter(request, response, chain);

		assertRejected();
		assertThat(store.consume("ticket-1")).isEmpty();
	}

	@Test
	@DisplayName("같은 티켓을 다시 쓰면 400이다")
	void replayedTicket_rejected() throws Exception {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));
		long now = System.currentTimeMillis();
		filter.doFilter(signed(LOGIN, BODY, "ticket-1", "signing-key", now), response, new MockFilterChain());

		MockHttpServletResponse secondResponse = new MockHttpServletResponse();
		MockFilterChain secondChain = new MockFilterChain();
		filter.doFilter(signed(LOGIN, BODY, "ticket-1", "signing-key", now), secondResponse, secondChain);

		assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(secondResponse.getContentAsString()).isEqualTo(FAIL_BODY);
		assertThat(secondChain.getRequest()).isNull();
	}

	@Test
	@DisplayName("서명 대상이 아닌 경로는 헤더 없이 통과한다")
	void unsignedPath_skipped() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/boards/qna");

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(chain.getRequest()).isSameAs(request);
	}

	@Test
	@DisplayName("티켓 발급 GET은 서명하지 않는다")
	void requestTicketGet_skipped() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/request-ticket");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
	}

	@Test
	@DisplayName("스위치가 꺼져 있으면 검증을 건너뛴다")
	void disabled_skipped() throws Exception {
		filter = new RequestSignatureFilter(new RequestSignaturePolicy(), store, false, 300);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", LOGIN);

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
	}

	@Test
	@DisplayName("끝 슬래시가 있어도 로그인 POST는 검증한다")
	void trailingSlash_stillVerified() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login/");
		request.setContent(BODY.getBytes(StandardCharsets.UTF_8));

		filter.doFilter(request, response, chain);

		assertRejected();
	}

	private MockHttpServletRequest signed(String path, String body, String ticketId, String signingKey, long timestamp) {
		byte[] raw = body.getBytes(StandardCharsets.UTF_8);
		String canonical = timestamp + "\nPOST\n" + path + "\n" + HmacSha256.sha256Hex(raw);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
		request.setContent(raw);
		request.addHeader("X-Hexaq-Timestamp", String.valueOf(timestamp));
		request.addHeader("X-Hexaq-Ticket", ticketId);
		request.addHeader("X-Hexaq-Signature", HmacSha256.hmacHex(signingKey, canonical));
		return request;
	}

	private MockHttpServletRequest withTimestamp(MockHttpServletRequest source, String timestamp) {
		MockHttpServletRequest request = new MockHttpServletRequest(source.getMethod(), source.getRequestURI());
		request.setContent(source.getContentAsByteArray());
		request.addHeader("X-Hexaq-Timestamp", timestamp);
		request.addHeader("X-Hexaq-Ticket", source.getHeader("X-Hexaq-Ticket"));
		request.addHeader("X-Hexaq-Signature", source.getHeader("X-Hexaq-Signature"));
		return request;
	}

	private void assertRejected() throws Exception {
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.getContentType()).contains("application/json");
		assertThat(response.getContentAsString()).isEqualTo(FAIL_BODY);
	}
}
