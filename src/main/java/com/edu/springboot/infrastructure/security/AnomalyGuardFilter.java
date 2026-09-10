package com.edu.springboot.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.OptionalLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edu.springboot.domain.anomaly.AnomalyPolicy;
import com.edu.springboot.domain.anomaly.AnomalySignalStore;
import com.edu.springboot.domain.anomaly.AnomalyStep;
import com.edu.springboot.domain.anomaly.AnomalyVerdict;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
@Order(0)
public class AnomalyGuardFilter extends OncePerRequestFilter {

	private static final String RATE_BODY =
		"{\"success\":false,\"data\":null,\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}";
	private static final String SESSION_BODY =
		"{\"success\":false,\"data\":null,\"message\":\"세션이 만료되었습니다. 다시 로그인해 주세요.\"}";

	private final AnomalyPolicy anomalyPolicy;
	private final AnomalySignalStore anomalySignalStore;
	private final MemberRepository memberRepository;
	private final boolean enabled;
	private final long ttfaMs;
	private final long newAccountAgeMs;
	private final int newUserLimit;
	private final Duration window;
	private final Duration lookback;
	private final Duration firstActionTtl;
	private final int directHitLimit;

	public AnomalyGuardFilter(
		AnomalyPolicy anomalyPolicy,
		AnomalySignalStore anomalySignalStore,
		MemberRepository memberRepository,
		@Value("${app.anomaly.enabled:true}") boolean enabled,
		@Value("${app.anomaly.ttfa-ms:3000}") long ttfaMs,
		@Value("${app.anomaly.new-user-age-hours:24}") int newUserAgeHours,
		@Value("${app.anomaly.new-user-limit:5}") int newUserLimit,
		@Value("${app.anomaly.new-user-window-seconds:60}") int windowSeconds,
		@Value("${app.anomaly.nav-lookback-seconds:300}") int lookbackSeconds,
		@Value("${app.anomaly.signup-ttl-hours:24}") int signupTtlHours,
		@Value("${app.anomaly.direct-hit-limit:1}") int directHitLimit
	) {
		this.anomalyPolicy = anomalyPolicy;
		this.anomalySignalStore = anomalySignalStore;
		this.memberRepository = memberRepository;
		this.enabled = enabled;
		this.ttfaMs = Math.max(0, ttfaMs);
		this.newAccountAgeMs = Math.max(1, newUserAgeHours) * 3_600_000L;
		this.newUserLimit = Math.max(1, newUserLimit);
		this.window = Duration.ofSeconds(Math.max(1, windowSeconds));
		this.lookback = Duration.ofSeconds(Math.max(1, lookbackSeconds));
		this.firstActionTtl = Duration.ofHours(Math.max(1, signupTtlHours));
		this.directHitLimit = Math.max(1, directHitLimit);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}
		return !AnomalyPaths.coreWrite(request.getMethod(), request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			filterChain.doFilter(request, response);
			return;
		}
		String loginId = authentication.getName();
		if (loginId == null || loginId.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}
		Member member = memberRepository.findByLoginId(loginId)
			.or(() -> memberRepository.findByEmail(loginId))
			.orElse(null);
		if (member == null) {
			filterChain.doFilter(request, response);
			return;
		}
		if (member.isAdmin()) {
			filterChain.doFilter(request, response);
			return;
		}
		if (member.suspicious()) {
			write(response, HttpStatus.TOO_MANY_REQUESTS, RATE_BODY);
			return;
		}
		if (!anomalySignalStore.hasStep(loginId, AnomalyStep.CONTEXT)) {
			expireSession(request);
			write(response, HttpStatus.UNAUTHORIZED, SESSION_BODY);
			return;
		}

		long now = System.currentTimeMillis();
		boolean hasRecentGet = anomalySignalStore.hasGetSince(loginId, now - lookback.toMillis());
		if (hasRecentGet) {
			anomalySignalStore.clearDirectCoreHits(loginId);
		}
		int directHits = hasRecentGet ? 0 : anomalySignalStore.incrementDirectCoreHits(loginId, lookback);
		boolean firstCore = anomalySignalStore.claimFirstCoreAction(loginId, firstActionTtl);
		OptionalLong signupAt = anomalySignalStore.signupAt(loginId);
		Long signupMillis = signupAt.isPresent() ? signupAt.getAsLong() : epoch(member);
		boolean newAccount = anomalyPolicy.newAccount(epoch(member), now, newAccountAgeMs);
		String apiName = AnomalyPaths.apiName(request.getMethod(), request.getRequestURI());
		long windowHits = newAccount ? anomalySignalStore.incrementWindow(loginId, apiName, window) : 0;

		AnomalyVerdict verdict = anomalyPolicy.decide(
			false,
			false,
			true,
			hasRecentGet,
			directHits,
			directHitLimit,
			firstCore,
			signupMillis,
			now,
			ttfaMs,
			newAccount,
			windowHits,
			newUserLimit
		);
		if (verdict == AnomalyVerdict.ALLOW) {
			filterChain.doFilter(request, response);
			return;
		}
		if (verdict == AnomalyVerdict.TTFA) {
			member.markSuspicious();
			memberRepository.save(member);
		}
		write(response, HttpStatus.TOO_MANY_REQUESTS, RATE_BODY);
	}

	private Long epoch(Member member) {
		if (member.getCreatedAt() == null) {
			return null;
		}
		return member.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private void expireSession(HttpServletRequest request) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	private void write(HttpServletResponse response, HttpStatus status, String body) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if (status == HttpStatus.TOO_MANY_REQUESTS) {
			response.setHeader("Retry-After", "60");
		}
		response.getWriter().write(body);
	}
}
