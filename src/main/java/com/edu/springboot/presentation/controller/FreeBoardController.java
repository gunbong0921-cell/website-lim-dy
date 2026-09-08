package com.edu.springboot.presentation.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

import com.edu.springboot.application.board.FreeBoardService;
import com.edu.springboot.application.board.LikeService;
import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.presentation.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards/free")
@RequiredArgsConstructor
public class FreeBoardController {

	private final FreeBoardService freeBoardService;
	private final LikeService likeService;
	private final ViewCountPolicy viewCountPolicy;

	@GetMapping
	public ApiResponse<PageResponse<BoardSummaryResponse>> list(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String searchType,
		@RequestParam(required = false) String keyword
	) {
		return ApiResponse.ok(freeBoardService.list(searchType, keyword, page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardDetailResponse>> read(
		@PathVariable Long id,
		jakarta.servlet.http.HttpServletRequest request
	) {
		String cookieName = viewCountPolicy.cookieName("FREE", id);
		boolean viewed = hasCookie(request, cookieName);
		BoardDetailResponse detail = freeBoardService.read(id, viewed);
		var body = ApiResponse.ok(detail);
		if (!viewed && detail.visitIncreased()) {
			ResponseCookie cookie = ResponseCookie.from(cookieName, "Y")
				.path("/")
				.maxAge(viewCountPolicy.cookieMaxAgeSeconds(LocalDateTime.now()))
				.httpOnly(false)
				.build();
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ApiResponse<Long> write(@RequestBody FreeWriteRequest request) {
		return ApiResponse.ok(freeBoardService.write(request.title(), request.content(), request.writer(), request.password()),
			"등록되었습니다.");
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> update(@PathVariable Long id, @RequestBody FreeWriteRequest request) {
		freeBoardService.update(id, request.title(), request.content(), request.password());
		return ApiResponse.ok(null, "수정되었습니다.");
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id, @RequestBody FreeWriteRequest request) {
		freeBoardService.delete(id, request.password());
		return ApiResponse.ok(null, "삭제되었습니다.");
	}

	@PostMapping("/{id}/like")
	public ApiResponse<Integer> like(@PathVariable Long id, Authentication authentication) {
		String loginId = (authentication == null || authentication instanceof AnonymousAuthenticationToken)
			? null
			: authentication.getName();
		return ApiResponse.ok(likeService.like("FREE", id, loginId), "좋아요가 반영되었습니다.");
	}

	private boolean hasCookie(jakarta.servlet.http.HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return false;
		}
		for (var cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) {
				return true;
			}
		}
		return false;
	}

	public record FreeWriteRequest(String title, String content, String writer, String password) {
	}
}
