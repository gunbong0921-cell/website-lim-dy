package com.edu.springboot.domain.anomaly;

/**
 * Hexaq
 * 계층: Domain
 * 객체: AnomalyPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class AnomalyPolicy {

	public AnomalyVerdict decide(
		boolean admin,
		boolean suspicious,
		boolean contextStep,
		boolean hasRecentGet,
		int directHitsWithoutGet,
		int directHitLimit,
		boolean firstCoreAction,
		Long signupAtMillis,
		long nowMillis,
		long ttfaMs,
		boolean newAccount,
		long windowHits,
		int newUserLimit
	) {
		if (admin) {
			return AnomalyVerdict.ALLOW;
		}
		if (suspicious) {
			return AnomalyVerdict.SUSPICIOUS;
		}
		if (!contextStep) {
			return AnomalyVerdict.SEQUENCE;
		}
		if (!hasRecentGet && directHitsWithoutGet >= directHitLimit) {
			return AnomalyVerdict.ZERO_NAV;
		}
		if (firstCoreAction && signupAtMillis != null && nowMillis - signupAtMillis < ttfaMs) {
			return AnomalyVerdict.TTFA;
		}
		if (newAccount && windowHits > newUserLimit) {
			return AnomalyVerdict.NEW_USER_RATE;
		}
		return AnomalyVerdict.ALLOW;
	}

	public boolean newAccount(Long createdAtMillis, long nowMillis, long maxAgeMillis) {
		if (createdAtMillis == null || maxAgeMillis <= 0) {
			return false;
		}
		return nowMillis - createdAtMillis < maxAgeMillis;
	}
}
