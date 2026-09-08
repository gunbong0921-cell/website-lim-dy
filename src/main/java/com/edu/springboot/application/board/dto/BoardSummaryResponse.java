package com.edu.springboot.application.board.dto;

import java.time.LocalDateTime;

public record BoardSummaryResponse(
	Long id,
	String title,
	String writer,
	int visitCount,
	int likeCount,
	LocalDateTime createdAt
) {
}
