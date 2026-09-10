package com.edu.springboot.domain.board;

import java.util.List;
import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: FreeBoardRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface FreeBoardRepository {

	FreeBoard save(FreeBoard board);

	Optional<FreeBoard> findById(Long id);

	void update(FreeBoard board);

	void delete(Long id);

	List<FreeBoard> findPage(String searchType, String keyword, int offset, int size);

	long count(String searchType, String keyword);

	void increaseVisitCount(Long id);

	void increaseLikeCount(Long id);
}
