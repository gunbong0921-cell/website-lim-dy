package com.edu.springboot.infrastructure.security;

public final class AnomalyPaths {

	private AnomalyPaths() {
	}

	public static boolean contextGet(String method, String path) {
		if (!"GET".equalsIgnoreCase(method) || path == null) {
			return false;
		}
		return "/api/members/me".equals(path) || path.startsWith("/api/boards/");
	}

	public static boolean coreWrite(String method, String path) {
		if (path == null || !isMutating(method)) {
			return false;
		}
		if (path.startsWith("/api/comments")) {
			return true;
		}
		if (path.startsWith("/api/boards/qna") || path.startsWith("/api/boards/archive")) {
			return true;
		}
		if (path.startsWith("/api/boards/free") && !"GET".equalsIgnoreCase(method)) {
			return true;
		}
		return path.contains("/like");
	}

	public static String apiName(String method, String path) {
		String verb = method == null ? "POST" : method.toUpperCase();
		if (path != null && path.contains("/like")) {
			return verb + ":like";
		}
		if (path != null && path.startsWith("/api/comments")) {
			return verb + ":comment";
		}
		if (path != null && path.startsWith("/api/boards/qna")) {
			return verb + ":qna";
		}
		if (path != null && path.startsWith("/api/boards/archive")) {
			return verb + ":archive";
		}
		if (path != null && path.startsWith("/api/boards/free")) {
			return verb + ":free";
		}
		return verb + ":other";
	}

	private static boolean isMutating(String method) {
		return "POST".equalsIgnoreCase(method)
			|| "PUT".equalsIgnoreCase(method)
			|| "DELETE".equalsIgnoreCase(method);
	}
}
