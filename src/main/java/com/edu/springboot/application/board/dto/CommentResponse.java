package com.edu.springboot.application.board.dto;

import java.time.LocalDateTime;

/**
 * Hexaq
 * 계층: Application
 * 객체: CommentResponse
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/features/09-archive-attachments.md](../../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public record CommentResponse(
	Long id,
	Long boardId,
	String writer,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
