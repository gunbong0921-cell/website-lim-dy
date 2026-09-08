package com.edu.springboot.infrastructure.persistence.mybatis;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.board.LikeRepository;

import lombok.RequiredArgsConstructor;

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
