package com.edu.springboot.application.board.dto;

import java.time.LocalDateTime;

public record CommentResponse(
	Long id,
	Long boardId,
	String writer,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
