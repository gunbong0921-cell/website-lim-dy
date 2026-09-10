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
import com.edu.springboot.application.board.LikeService;
import com.edu.springboot.application.board.QnaBoardService;
import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.board.dto.CookieInstruction;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.presentation.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards/qna")
@RequiredArgsConstructor
public class QnaBoardController {

	private final QnaBoardService qnaBoardService;
	private final LikeService likeService;
	private final BoardCookieService boardCookieService;

	@GetMapping
	public ApiResponse<PageResponse<BoardSummaryResponse>> list(
		@RequestParam("solution") String solution,
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(name = "searchType", required = false) String searchType,
		@RequestParam(name = "keyword", required = false) String keyword
	) {
		return ApiResponse.ok(qnaBoardService.list(solution, searchType, keyword, page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardDetailResponse>> read(@PathVariable("id") Long id,
		@RequestParam(name = "solution", required = false) String solution, HttpServletRequest request) {
		CookieInstruction cookie = boardCookieService.viewCookie("QNA", id);
		boolean viewed = CookieSupport.has(request, cookie.name());
		BoardDetailResponse detail = qnaBoardService.read(id, solution, viewed);
		var body = ApiResponse.ok(detail);
		if (!viewed && detail.visitIncreased()) {
			ResponseCookie setCookie = CookieSupport.viewedToday(cookie.name(), cookie.maxAgeSeconds());
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, setCookie.toString()).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ApiResponse<Long> write(Authentication authentication, @RequestBody BoardWriteRequest request) {
		return ApiResponse.ok(
			qnaBoardService.write(request.title(), request.content(), authentication.getName(), request.solution()),
			"등록되었습니다.");
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> update(@PathVariable("id") Long id, Authentication authentication,
		@RequestBody BoardWriteRequest request) {
		qnaBoardService.update(id, request.title(), request.content(), authentication.getName());
		return ApiResponse.ok(null, "수정되었습니다.");
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id, Authentication authentication) {
		qnaBoardService.delete(id, authentication.getName());
		return ApiResponse.ok(null, "삭제되었습니다.");
	}

	@PostMapping("/{id}/like")
	public ApiResponse<Integer> like(@PathVariable("id") Long id, Authentication authentication) {
		return ApiResponse.ok(likeService.like("QNA", id, AuthSupport.loginId(authentication), false).count(),
			"좋아요가 반영되었습니다.");
	}

	public record BoardWriteRequest(String title, String content, String solution) {
	}
}
