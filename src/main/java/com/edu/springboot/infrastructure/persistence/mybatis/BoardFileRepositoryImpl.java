package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.BoardFile;
import com.edu.springboot.domain.board.BoardFileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: BoardFileRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class BoardFileRepositoryImpl implements BoardFileRepository {

	private final BoardFileMapper mapper;

	@Override
	public void save(BoardFile file) {
		mapper.insert(file);
	}

	@Override
	public List<BoardFile> findByBoardId(Long boardId) {
		return mapper.findByBoardId(boardId);
	}

	@Override
	public Optional<BoardFile> findById(Long id) {
		return Optional.ofNullable(mapper.findById(id));
	}

	@Override
	public void deleteByBoardId(Long boardId) {
		mapper.deleteByBoardId(boardId);
	}
}
