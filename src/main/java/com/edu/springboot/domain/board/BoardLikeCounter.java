package com.edu.springboot.domain.board;

import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: BoardLikeCounter
 * 책임: BoardLikeCounter 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface BoardLikeCounter {

	String boardType();

	Optional<Integer> increaseAndCount(Long boardId);
}
