package com.edu.springboot.domain.board;

import lombok.Getter;
import lombok.Setter;

/**
 * Hexaq
 * 계층: Domain
 * 객체: BoardFile
 * 책임: BoardFile 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Getter
@Setter
public class BoardFile {

	private Long id;
	private Long boardId;
	private String originalName;
	private String storedName;
	private String contentType;
	private Long fileSize;
	private String fileType;
}
