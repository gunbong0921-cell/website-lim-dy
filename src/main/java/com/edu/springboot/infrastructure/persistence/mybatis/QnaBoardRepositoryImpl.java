package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.QnaBoard;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

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
