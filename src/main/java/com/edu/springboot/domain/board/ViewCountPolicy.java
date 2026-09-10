package com.edu.springboot.domain.board;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Hexaq
 * 계층: Domain
 * 객체: ViewCountPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class ViewCountPolicy {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	public boolean shouldIncrease(boolean alreadyViewedToday) {
		return !alreadyViewedToday;
	}

	public String cookieName(String boardType, Long boardId) {
		return "viewed_" + boardType + "_" + boardId;
	}

	public int cookieMaxAgeSeconds(LocalDateTime now) {
		LocalDateTime midnight = LocalDate.now(ZONE).plusDays(1).atStartOfDay();
		long seconds = Duration.between(now, midnight).getSeconds();
		return (int) Math.max(1, seconds);
	}
}
