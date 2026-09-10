package com.edu.springboot.application.board;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.board.dto.CookieInstruction;
import com.edu.springboot.domain.board.LikePolicy;
import com.edu.springboot.domain.board.ViewCountPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: BoardCookieService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
public class BoardCookieService {

	private final ViewCountPolicy viewCountPolicy;
	private final LikePolicy likePolicy;

	public CookieInstruction viewCookie(String boardType, Long boardId) {
		return new CookieInstruction(
			viewCountPolicy.cookieName(boardType, boardId),
			viewCountPolicy.cookieMaxAgeSeconds(LocalDateTime.now())
		);
	}

	public CookieInstruction guestLikeCookie(String boardType, Long boardId) {
		return new CookieInstruction(
			likePolicy.cookieName(boardType, boardId),
			likePolicy.cookieMaxAgeSeconds()
		);
	}
}
