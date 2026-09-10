package com.edu.springboot.infrastructure.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;
import com.edu.springboot.domain.anomaly.AnomalyStep;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: AnomalyNavigationInterceptor
 * 책임: Spring MVC 인터셉터
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
public class AnomalyNavigationInterceptor implements HandlerInterceptor {

	private final AnomalySignalStore anomalySignalStore;
	private final boolean enabled;
	private final Duration lookback;
	private final Duration stepTtl;
	private final int navMaxEntries;

	public AnomalyNavigationInterceptor(
		AnomalySignalStore anomalySignalStore,
		@Value("${app.anomaly.enabled:true}") boolean enabled,
		@Value("${app.anomaly.nav-lookback-seconds:300}") int lookbackSeconds,
		@Value("${app.anomaly.step-ttl-hours:24}") int stepTtlHours,
		@Value("${app.anomaly.nav-max-entries:50}") int navMaxEntries
	) {
		this.anomalySignalStore = anomalySignalStore;
		this.enabled = enabled;
		this.lookback = Duration.ofSeconds(Math.max(1, lookbackSeconds));
		this.stepTtl = Duration.ofHours(Math.max(1, stepTtlHours));
		this.navMaxEntries = Math.max(1, navMaxEntries);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!enabled) {
			return true;
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			return true;
		}
		String loginId = authentication.getName();
		if (loginId == null || loginId.isBlank()) {
			return true;
		}
		String path = request.getRequestURI();
		if (!AnomalyPaths.contextGet(request.getMethod(), path)) {
			return true;
		}
		anomalySignalStore.pushGet(loginId, path, lookback, navMaxEntries);
		anomalySignalStore.clearDirectCoreHits(loginId);
		if ("/api/members/me".equals(path)) {
			anomalySignalStore.markStep(loginId, AnomalyStep.CONTEXT, stepTtl);
		}
		return true;
	}
}
