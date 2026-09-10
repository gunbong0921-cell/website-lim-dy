package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.QnaBoard;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: QnaBoardRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class QnaBoardRepositoryImpl implements QnaBoardRepository {

	private final QnaBoardMapper mapper;

	@Override
	public QnaBoard save(QnaBoard board) {
		mapper.insert(board);
		return board;
	}

	@Override
	public Optional<QnaBoard> findById(Long id) {
		return Optional.ofNullable(mapper.findById(id));
	}

	@Override
	public void update(QnaBoard board) {
		mapper.update(board);
	}

	@Override
	public void delete(Long id) {
		mapper.delete(id);
	}

	@Override
	public List<QnaBoard> findPage(String solution, String searchType, String keyword, int offset, int size) {
		return mapper.findPage(solution, searchType, keyword, offset, size);
	}

	@Override
	public long count(String solution, String searchType, String keyword) {
		return mapper.count(solution, searchType, keyword);
	}

	@Override
	public void increaseVisitCount(Long id) {
		mapper.increaseVisitCount(id);
	}

	@Override
	public void increaseLikeCount(Long id) {
		mapper.increaseLikeCount(id);
	}
}
