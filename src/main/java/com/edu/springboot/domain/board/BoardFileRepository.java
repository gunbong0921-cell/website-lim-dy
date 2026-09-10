package com.edu.springboot.domain.board;

import java.util.List;

/**
 * Hexaq
 * 계층: Domain
 * 객체: BoardFileRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface BoardFileRepository {

	void save(BoardFile file);

	List<BoardFile> findByBoardId(Long boardId);

	java.util.Optional<BoardFile> findById(Long id);

	void deleteByBoardId(Long boardId);
}
