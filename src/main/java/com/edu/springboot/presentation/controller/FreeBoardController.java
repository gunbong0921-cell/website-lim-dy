package com.edu.springboot.presentation.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.board.BoardCookieService;
import com.edu.springboot.application.board.FreeBoardService;
import com.edu.springboot.application.board.LikeService;
import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.board.dto.CookieInstruction;
import com.edu.springboot.application.board.dto.LikeResult;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.presentation.dto.ApiResponse;
import com.edu.springboot.presentation.http.RequestClientIp;
import com.edu.springboot.presentation.http.RequestHostname;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards/free")
@RequiredArgsConstructor
public class FreeBoardController {

	private final FreeBoardService freeBoardService;
	private final LikeService likeService;
	private final BoardCookieService boardCookieService;
	private final RequestClientIp requestClientIp;
	private final RequestHostname requestHostname;

	@GetMapping
	public ApiResponse<PageResponse<BoardSummaryResponse>> list(
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(name = "searchType", required = false) String searchType,
		@RequestParam(name = "keyword", required = false) String keyword
	) {
		return ApiResponse.ok(freeBoardService.list(searchType, keyword, page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardDetailResponse>> read(
		@PathVariable("id") Long id,
		jakarta.servlet.http.HttpServletRequest request
	) {
		CookieInstruction cookie = boardCookieService.viewCookie("FREE", id);
		boolean viewed = CookieSupport.has(request, cookie.name());
		BoardDetailResponse detail = freeBoardService.read(id, viewed);
		var body = ApiResponse.ok(detail);
		if (!viewed && detail.visitIncreased()) {
			ResponseCookie setCookie = CookieSupport.viewedToday(cookie.name(), cookie.maxAgeSeconds());
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, setCookie.toString()).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ApiResponse<Long> write(@RequestBody FreeWriteRequest request,
		jakarta.servlet.http.HttpServletRequest httpRequest) {
		return ApiResponse.ok(freeBoardService.write(request.title(), request.content(), request.writer(),
			request.password(), request.recaptchaToken(), requestClientIp.resolve(httpRequest),
			requestHostname.resolve(httpRequest)),
			"등록되었습니다.");
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody FreeWriteRequest request) {
		freeBoardService.update(id, request.title(), request.content(), request.password());
		return ApiResponse.ok(null, "수정되었습니다.");
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id, @RequestBody FreeWriteRequest request) {
		freeBoardService.delete(id, request.password());
		return ApiResponse.ok(null, "삭제되었습니다.");
	}

	@PostMapping("/{id}/like")
	public ResponseEntity<ApiResponse<Integer>> like(
		@PathVariable("id") Long id,
		Authentication authentication,
		jakarta.servlet.http.HttpServletRequest request
	) {
		String loginId = AuthSupport.loginId(authentication);
		CookieInstruction cookie = boardCookieService.guestLikeCookie("FREE", id);
		boolean alreadyLiked = CookieSupport.has(request, cookie.name());
		LikeResult result = likeService.like("FREE", id, loginId, alreadyLiked);
		var body = ApiResponse.ok(result.count(), "좋아요가 반영되었습니다.");
		if (result.setGuestCookie()) {
			ResponseCookie setCookie = CookieSupport.viewedToday(cookie.name(), cookie.maxAgeSeconds());
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, setCookie.toString()).body(body);
		}
		return ResponseEntity.ok(body);
	}

	public record FreeWriteRequest(String title, String content, String writer, String password, String recaptchaToken) {
	}
}
