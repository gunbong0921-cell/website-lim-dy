package com.edu.springboot.domain.anomaly;

import java.time.Duration;
import java.util.OptionalLong;

/**
 * Hexaq
 * 계층: Domain
 * 객체: AnomalySignalStore
 * 책임: AnomalySignalStore 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface AnomalySignalStore {

	void rememberSignup(String loginId, long epochMilli, Duration ttl);

	OptionalLong signupAt(String loginId);

	boolean claimFirstCoreAction(String loginId, Duration ttl);

	long incrementWindow(String loginId, String apiName, Duration window);

	void pushGet(String loginId, String route, Duration lookback, int maxEntries);

	boolean hasGetSince(String loginId, long sinceEpochMilli);

	int incrementDirectCoreHits(String loginId, Duration ttl);

	void clearDirectCoreHits(String loginId);

	void markStep(String loginId, String step, Duration ttl);

	boolean hasStep(String loginId, String step);
}
