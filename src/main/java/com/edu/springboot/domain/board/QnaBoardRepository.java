package com.edu.springboot.domain.board;

import java.util.List;
import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: QnaBoardRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface QnaBoardRepository {

	QnaBoard save(QnaBoard board);

	Optional<QnaBoard> findById(Long id);

	void update(QnaBoard board);

	void delete(Long id);

	List<QnaBoard> findPage(String solution, String searchType, String keyword, int offset, int size);

	long count(String solution, String searchType, String keyword);

	void increaseVisitCount(Long id);

	void increaseLikeCount(Long id);
}
