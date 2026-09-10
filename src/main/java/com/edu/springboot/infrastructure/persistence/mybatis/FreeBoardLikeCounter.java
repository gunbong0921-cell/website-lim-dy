package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.FreeBoardRepository;

import lombok.RequiredArgsConstructor;

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
