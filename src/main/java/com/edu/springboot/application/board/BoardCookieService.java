package com.edu.springboot.application.board;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.board.dto.CookieInstruction;
import com.edu.springboot.domain.board.LikePolicy;
import com.edu.springboot.domain.board.ViewCountPolicy;

import lombok.RequiredArgsConstructor;

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
