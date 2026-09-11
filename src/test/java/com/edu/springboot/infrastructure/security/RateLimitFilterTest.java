package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.edu.springboot.domain.security.ClientAddressPolicy;

/**
 * Hexaq
 * 계층: Test
 * 객체: RateLimitFilterTest
 * 책임: IP+경로 버킷, 정적 제외, 429·Retry-After
 * 문서: [docs/security/02-rate-limiting.md](../../../../../../../../docs/security/02-rate-limiting.md)
 */
class RateLimitFilterTest {

	private static final String FAIL_BODY =
		"{\"success\":false,\"data\":null,\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}";

	private RateLimitFilter filter;
	private MockHttpServletResponse lastResponse;
	private MockFilterChain lastChain;

	@BeforeEach
	void setUp() {
		filter = newFilter(false, true, 60, 3, 2, 4);
	}

	@Test
	@DisplayName("인증 버킷은 한도를 넘으면 429이다")
	void authBucket_rejectsOverLimit() throws Exception {
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertAllowed(hit("POST", "/api/members/signup", "1.1.1.1"));
		assertRejected(hit("POST", "/api/auth/forgot-id", "1.1.1.1"));
	}

	@Test
	@DisplayName("API 버킷은 인증과 따로 센다")
	void apiBucket_isSeparateFromAuth() throws Exception {
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertRejected(hit("GET", "/api/auth/login", "1.1.1.1"));

		assertAllowed(hit("GET", "/api/boards/free", "1.1.1.1"));
		assertAllowed(hit("GET", "/api/public/config", "1.1.1.1"));
		assertAllowed(hit("GET", "/api/boards/qna", "1.1.1.1"));
		assertRejected(hit("GET", "/api/public/request-ticket", "1.1.1.1"));
	}

	@Test
	@DisplayName("페이지 버킷은 API와 따로 센다")
	void pageBucket_isSeparateFromApi() throws Exception {
		assertAllowed(hit("GET", "/", "1.1.1.1"));
		assertAllowed(hit("GET", "/login", "1.1.1.1"));
		assertAllowed(hit("GET", "/boards/free", "1.1.1.1"));
		assertAllowed(hit("GET", "/qna", "1.1.1.1"));
		assertRejected(hit("GET", "/", "1.1.1.1"));
	}

	@Test
	@DisplayName("IP가 다르면 같은 경로도 따로 센다")
	void differentIp_hasOwnBucket() throws Exception {
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertRejected(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertAllowed(hit("POST", "/api/auth/login", "2.2.2.2"));
	}

	@Test
	@DisplayName("초과 응답은 429와 Retry-After와 본문이다")
	void overLimit_setsRetryAfterAndBody() throws Exception {
		hit("POST", "/api/auth/login", "1.1.1.1");
		hit("POST", "/api/auth/login", "1.1.1.1");
		HitResult over = hit("POST", "/api/auth/login", "1.1.1.1");

		assertThat(over.response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(over.response.getHeader("Retry-After")).isEqualTo("60");
		assertThat(over.response.getContentType()).contains("application/json");
		assertThat(over.response.getContentAsString()).isEqualTo(FAIL_BODY);
		assertThat(over.chain.getRequest()).isNull();
	}

	@Test
	@DisplayName("정적 파일은 한도를 보지 않는다")
	void staticAsset_skipped() throws Exception {
		HitResult result = hit("GET", "/assets/app.js", "1.1.1.1");

		assertAllowed(result);
		assertThat(result.chain.getRequest()).isSameAs(result.request);
	}

	@Test
	@DisplayName("index.html과 업로드는 한도를 보지 않는다")
	void indexAndUploads_skipped() throws Exception {
		assertAllowed(hit("GET", "/index.html", "1.1.1.1"));
		assertAllowed(hit("GET", "/uploads/file.png", "1.1.1.1"));
	}

	@Test
	@DisplayName("스위치가 꺼져 있으면 검증을 건너뛴다")
	void disabled_skipped() throws Exception {
		filter = newFilter(false, false, 60, 1, 1, 1);

		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
	}

	@Test
	@DisplayName("프록시를 믿지 않으면 같은 X-Forwarded-For여도 IP를 나눈다")
	void untrustedProxy_ignoresSpoofedForwardedFor() throws Exception {
		HitResult first = hitWithForwarded("1.1.1.1", "9.9.9.9");
		HitResult second = hitWithForwarded("1.1.1.1", "9.9.9.9");
		HitResult other = hitWithForwarded("2.2.2.2", "9.9.9.9");

		assertAllowed(first);
		assertAllowed(second);
		assertAllowed(other);
		assertRejected(hitWithForwarded("1.1.1.1", "9.9.9.9"));
	}

	@Test
	@DisplayName("신뢰 프록시면 X-Forwarded-For 첫 주소로 버킷을 묶는다")
	void trustedProxy_groupsByForwardedFor() throws Exception {
		filter = newFilter(true, true, 60, 3, 2, 4);

		assertAllowed(hitWithForwarded("10.0.0.1", "203.0.113.10, 10.0.0.1"));
		assertAllowed(hitWithForwarded("10.0.0.2", "203.0.113.10, 10.0.0.2"));
		assertRejected(hitWithForwarded("10.0.0.3", "203.0.113.10, 10.0.0.3"));
	}

	@Test
	@DisplayName("OAuth와 인증번호 경로는 인증 버킷이다")
	void oauthAndVerifyPaths_useAuthBucket() throws Exception {
		assertAllowed(hit("GET", "/oauth2/authorization/google", "1.1.1.1"));
		assertAllowed(hit("GET", "/login/oauth2/code/google", "1.1.1.1"));
		assertRejected(hit("POST", "/api/members/email/send-code", "1.1.1.1"));
	}

	@Test
	@DisplayName("창이 지나면 다시 허용한다")
	void windowExpiry_allowsAgain() throws Exception {
		filter = newFilter(false, true, 1, 3, 1, 4);
		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
		assertRejected(hit("POST", "/api/auth/login", "1.1.1.1"));

		Thread.sleep(1_100L);

		assertAllowed(hit("POST", "/api/auth/login", "1.1.1.1"));
	}

	private RateLimitFilter newFilter(
		boolean trustedProxy,
		boolean enabled,
		int windowSeconds,
		int apiLimit,
		int authLimit,
		int pageLimit
	) {
		return new RateLimitFilter(
			new ClientAddressPolicy(trustedProxy),
			enabled,
			windowSeconds,
			apiLimit,
			authLimit,
			pageLimit
		);
	}

	private HitResult hit(String method, String path, String ip) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(method, path);
		request.setRemoteAddr(ip);
		return filter(request);
	}

	private HitResult hitWithForwarded(String remoteAddr, String forwardedFor) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
		request.setRemoteAddr(remoteAddr);
		request.addHeader("X-Forwarded-For", forwardedFor);
		return filter(request);
	}

	private HitResult filter(MockHttpServletRequest request) throws Exception {
		lastResponse = new MockHttpServletResponse();
		lastChain = new MockFilterChain();
		this.filter.doFilter(request, lastResponse, lastChain);
		return new HitResult(request, lastResponse, lastChain);
	}

	private void assertAllowed(HitResult result) {
		assertThat(result.response.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(result.chain.getRequest()).isSameAs(result.request);
	}

	private void assertRejected(HitResult result) throws Exception {
		assertThat(result.response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(result.response.getContentAsString()).isEqualTo(FAIL_BODY);
		assertThat(result.chain.getRequest()).isNull();
	}

	private record HitResult(
		MockHttpServletRequest request,
		MockHttpServletResponse response,
		MockFilterChain chain
	) {
	}
}
