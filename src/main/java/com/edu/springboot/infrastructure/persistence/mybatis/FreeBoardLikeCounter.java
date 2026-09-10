package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.FreeBoardRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: FreeBoardLikeCounter
 * 책임: FreeBoardLikeCounter 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@RequiredArgsConstructor
public class FreeBoardLikeCounter implements BoardLikeCounter {

	private final FreeBoardRepository freeBoardRepository;

	@Override
	public String boardType() {
		return "FREE";
	}

	@Override
	public Optional<Integer> increaseAndCount(Long boardId) {
		return freeBoardRepository.findById(boardId).map(board -> {
			freeBoardRepository.increaseLikeCount(boardId);
			return board.getLikeCount() + 1;
		});
	}
}
