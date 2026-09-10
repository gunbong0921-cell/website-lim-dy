package com.edu.springboot.domain.board;

/**
 * Hexaq
 * 계층: Domain
 * 객체: LikePolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
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
