package com.edu.springboot.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edu.springboot.domain.security.ClientAddressPolicy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: RateLimitFilter
 * 책임: 서블릿 필터. Infrastructure 만 SecurityContext 참조
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String BODY = "{\"success\":false,\"data\":null,\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}";
	private static final int MAX_KEYS = 20_000;

	private final ClientAddressPolicy clientAddressPolicy;
	private final boolean enabled;
	private final long windowMs;
	private final int apiLimit;
	private final int authLimit;
	private final int pageLimit;
	private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

	public RateLimitFilter(
		ClientAddressPolicy clientAddressPolicy,
		@Value("${app.security.rate-limit.enabled:true}") boolean enabled,
		@Value("${app.security.rate-limit.window-seconds:60}") int windowSeconds,
		@Value("${app.security.rate-limit.api-per-window:60}") int apiLimit,
		@Value("${app.security.rate-limit.auth-per-window:10}") int authLimit,
		@Value("${app.security.rate-limit.page-per-window:300}") int pageLimit
	) {
		this.clientAddressPolicy = clientAddressPolicy;
		this.enabled = enabled;
		this.windowMs = Math.max(1, windowSeconds) * 1000L;
		this.apiLimit = Math.max(1, apiLimit);
		this.authLimit = Math.max(1, authLimit);
		this.pageLimit = Math.max(1, pageLimit);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}
		String path = request.getRequestURI();
		return isStatic(path);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String path = request.getRequestURI();
		int limit = limitFor(path);
		String key = limit + ":" + clientIp(request) + ":" + bucket(path);
		if (!allow(key, limit)) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setHeader("Retry-After", String.valueOf(windowMs / 1000));
			response.getWriter().write(BODY);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private int limitFor(String path) {
		if (isAuthPath(path)) {
			return authLimit;
		}
		if (path.startsWith("/api/")) {
			return apiLimit;
		}
		return pageLimit;
	}

	private String bucket(String path) {
		if (isAuthPath(path)) {
			return "auth";
		}
		if (path.startsWith("/api/")) {
			return "api";
		}
		return "page";
	}

	private boolean isAuthPath(String path) {
		return path.startsWith("/api/auth/")
			|| path.startsWith("/api/members/email/")
			|| path.startsWith("/api/members/phone/")
			|| path.startsWith("/oauth2/")
			|| path.startsWith("/login/oauth2/")
			|| "/api/members/signup".equals(path)
			|| "/api/members/resend-verification".equals(path)
			|| "/api/members/verify-email".equals(path)
			|| "/api/members/business/verify".equals(path)
			|| "/api/members/check-id".equals(path)
			|| "/api/members/check-email".equals(path);
	}

	private boolean isStatic(String path) {
		return path.startsWith("/assets/")
			|| path.startsWith("/uploads/")
			|| path.endsWith(".js")
			|| path.endsWith(".css")
			|| path.endsWith(".map")
			|| path.endsWith(".ico")
			|| path.endsWith(".png")
			|| path.endsWith(".jpg")
			|| path.endsWith(".jpeg")
			|| path.endsWith(".gif")
			|| path.endsWith(".webp")
			|| path.endsWith(".svg")
			|| path.endsWith(".woff")
			|| path.endsWith(".woff2")
			|| path.endsWith(".ttf")
			|| "/index.html".equals(path);
	}

	private String clientIp(HttpServletRequest request) {
		return clientAddressPolicy.resolve(
			request.getRemoteAddr(),
			request.getHeader("X-Forwarded-For"),
			request.getHeader("X-Real-IP")
		);
	}

	private boolean allow(String key, int limit) {
		long now = System.currentTimeMillis();
		if (hits.size() >= MAX_KEYS && !hits.containsKey(key)) {
			evictExpired(now);
			if (hits.size() >= MAX_KEYS) {
				return true;
			}
		}
		Deque<Long> deque = hits.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
		synchronized (deque) {
			while (!deque.isEmpty() && now - deque.peekFirst() >= windowMs) {
				deque.pollFirst();
			}
			if (deque.size() >= limit) {
				return false;
			}
			deque.addLast(now);
			return true;
		}
	}

	private void evictExpired(long now) {
		hits.forEach((key, deque) -> {
			synchronized (deque) {
				while (!deque.isEmpty() && now - deque.peekFirst() >= windowMs) {
					deque.pollFirst();
				}
				if (deque.isEmpty()) {
					hits.remove(key, deque);
				}
			}
		});
	}
}
