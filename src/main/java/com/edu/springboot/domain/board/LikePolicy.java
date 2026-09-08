package com.edu.springboot.domain.board;

public class LikePolicy {

	private static final int GUEST_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

	public boolean allowsGuest(String boardType) {
		return "FREE".equalsIgnoreCase(boardType);
	}

	public String cookieName(String boardType, Long boardId) {
		return "liked_" + boardType + "_" + boardId;
	}

	public int cookieMaxAgeSeconds() {
		return GUEST_COOKIE_MAX_AGE;
	}
}
