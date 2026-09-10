package com.edu.springboot.domain.board;

import java.util.Locale;
import java.util.Set;

/**
 * Hexaq
 * 계층: Domain
 * 객체: QnaSolution
 * 책임: QnaSolution 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public final class QnaSolution {

	private static final Set<String> ALLOWED = Set.of("web", "mobile", "ai", "fintech", "general");

	private QnaSolution() {
	}

	public static boolean isAllowed(String value) {
		return value != null && ALLOWED.contains(normalize(value));
	}

	public static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}
