package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: QnaBoardLikeCounter
 * 책임: QnaBoardLikeCounter 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@RequiredArgsConstructor
public class QnaBoardLikeCounter implements BoardLikeCounter {

	private final QnaBoardRepository qnaBoardRepository;

	@Override
	public String boardType() {
		return "QNA";
	}

	@Override
	public Optional<Integer> increaseAndCount(Long boardId) {
		return qnaBoardRepository.findById(boardId).map(board -> {
			qnaBoardRepository.increaseLikeCount(boardId);
			return board.getLikeCount() + 1;
		});
	}
}
