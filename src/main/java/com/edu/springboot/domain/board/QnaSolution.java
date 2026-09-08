package com.edu.springboot.domain.board;

import java.util.Locale;
import java.util.Set;

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
