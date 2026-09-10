package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

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
