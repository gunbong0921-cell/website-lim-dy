package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.Comment;
import com.edu.springboot.domain.board.CommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: CommentRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

	private final CommentMapper mapper;

	@Override
	public Comment save(Comment comment) {
		mapper.insert(comment);
		return comment;
	}

	@Override
	public Optional<Comment> findById(Long id) {
		return Optional.ofNullable(mapper.findById(id));
	}

	@Override
	public List<Comment> findByBoardId(Long boardId) {
		return mapper.findByBoardId(boardId);
	}

	@Override
	public void update(Comment comment) {
		mapper.update(comment);
	}

	@Override
	public void delete(Long id) {
		mapper.delete(id);
	}

	@Override
	public void deleteByBoardId(Long boardId) {
		mapper.deleteByBoardId(boardId);
	}
}
