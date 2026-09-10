package com.edu.springboot.domain.anomaly;

/**
 * Hexaq
 * 계층: Domain
 * 객체: AnomalyVerdict
 * 책임: AnomalyVerdict 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum AnomalyVerdict {
	ALLOW,
	TTFA,
	NEW_USER_RATE,
	ZERO_NAV,
	SEQUENCE,
	SUSPICIOUS
}
