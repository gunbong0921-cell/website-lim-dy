package com.edu.springboot.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edu.springboot.domain.security.RequestSignaturePolicy;
import com.edu.springboot.domain.security.RequestTicketStore;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestSignatureFilter extends OncePerRequestFilter {

	private static final String FAIL_BODY = "{\"success\":false,\"data\":null,\"message\":\"요청이 유효하지 않습니다.\"}";
	private static final Set<String> SIGNED_PATHS = Set.of(
		"/api/auth/login",
		"/api/members/signup",
		"/api/members/email/send-code",
		"/api/members/phone/send-code",
		"/api/auth/forgot-id",
		"/api/auth/forgot-password",
		"/api/boards/free"
	);

	private final RequestSignaturePolicy requestSignaturePolicy;
	private final RequestTicketStore requestTicketStore;
	private final boolean enabled;
	private final long maxSkewMillis;

	public RequestSignatureFilter(
		RequestSignaturePolicy requestSignaturePolicy,
		RequestTicketStore requestTicketStore,
		@Value("${app.request-signing.enabled:true}") boolean enabled,
		@Value("${app.request-signing.max-skew-seconds:300}") int maxSkewSeconds
	) {
		this.requestSignaturePolicy = requestSignaturePolicy;
		this.requestTicketStore = requestTicketStore;
		this.enabled = enabled;
		this.maxSkewMillis = Math.max(1, maxSkewSeconds) * 1000L;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		return !SIGNED_PATHS.contains(normalize(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
		if (!verified(cached)) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write(FAIL_BODY);
			return;
		}
		filterChain.doFilter(cached, response);
	}

	private boolean verified(CachedBodyHttpServletRequest request) {
		String timestamp = header(request, "X-Hexaq-Timestamp");
		String ticketId = header(request, "X-Hexaq-Ticket");
		String signature = header(request, "X-Hexaq-Signature");
		if (timestamp.isBlank() || ticketId.isBlank() || signature.isBlank()) {
			return false;
		}
		long timestampMillis;
		try {
			timestampMillis = Long.parseLong(timestamp);
		} catch (NumberFormatException ex) {
			return false;
		}
		if (!requestSignaturePolicy.timestampInWindow(timestampMillis, System.currentTimeMillis(), maxSkewMillis)) {
			return false;
		}
		Optional<String> signingKey = requestTicketStore.consume(ticketId);
		if (signingKey.isEmpty()) {
			return false;
		}
		String bodyHash = HmacSha256.sha256Hex(request.cachedBody());
		String canonical = timestamp + "\n" + request.getMethod().toUpperCase() + "\n"
			+ normalize(request.getRequestURI()) + "\n" + bodyHash;
		String expected = HmacSha256.hmacHex(signingKey.get(), canonical);
		return requestSignaturePolicy.signaturesMatch(expected, signature);
	}

	private String header(HttpServletRequest request, String name) {
		String value = request.getHeader(name);
		return value == null ? "" : value.trim();
	}

	private String normalize(String uri) {
		if (uri == null || uri.isBlank()) {
			return "";
		}
		if (uri.length() > 1 && uri.endsWith("/")) {
			return uri.substring(0, uri.length() - 1);
		}
		return uri;
	}
}
