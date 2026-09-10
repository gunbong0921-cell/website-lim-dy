package com.edu.springboot.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.board.CommentService;
import com.edu.springboot.application.board.dto.CommentResponse;
import com.edu.springboot.presentation.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: CommentController
 * 책임: HTTP 입구. 검증·Application 호출·ApiResponse. Domain 객체 비노출
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/{boardId}")
	public ApiResponse<List<CommentResponse>> list(@PathVariable("boardId") Long boardId) {
		return ApiResponse.ok(commentService.list(boardId));
	}

	@PostMapping("/{boardId}")
	public ApiResponse<CommentResponse> write(@PathVariable("boardId") Long boardId, Authentication authentication,
		@RequestBody Map<String, String> body) {
		return ApiResponse.ok(commentService.write(boardId, authentication.getName(), body.get("content")), "댓글이 등록되었습니다.");
	}

	@PutMapping("/item/{commentId}")
	public ApiResponse<CommentResponse> update(@PathVariable("commentId") Long commentId, Authentication authentication,
		@RequestBody Map<String, String> body) {
		return ApiResponse.ok(commentService.update(commentId, authentication.getName(), body.get("content")), "댓글이 수정되었습니다.");
	}

	@DeleteMapping("/item/{commentId}")
	public ApiResponse<Void> delete(@PathVariable("commentId") Long commentId, Authentication authentication) {
		commentService.delete(commentId, authentication.getName());
		return ApiResponse.ok(null, "댓글이 삭제되었습니다.");
	}
}
