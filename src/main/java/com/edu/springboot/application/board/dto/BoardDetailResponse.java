package com.edu.springboot.application.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDetailResponse(
	Long id,
	String title,
	String content,
	String writer,
	int visitCount,
	int likeCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	boolean visitIncreased,
	List<FileResponse> files
) {
}
