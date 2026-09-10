package com.edu.springboot.infrastructure.persistence.mybatis;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.LikeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: LikeRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

	private final LikeMapper mapper;

	@Override
	public boolean exists(String boardType, Long boardId, String memberLoginId) {
		return mapper.exists(boardType, boardId, memberLoginId) > 0;
	}

	@Override
	public void insert(String boardType, Long boardId, String memberLoginId) {
		mapper.insert(boardType, boardId, memberLoginId);
	}
}
