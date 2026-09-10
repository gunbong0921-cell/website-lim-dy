package com.edu.springboot.infrastructure.persistence.mybatis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.ArchiveBoardRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArchiveBoardLikeCounter implements BoardLikeCounter {

	private final ArchiveBoardRepository archiveBoardRepository;

	@Override
	public String boardType() {
		return "ARCHIVE";
	}

	@Override
	public Optional<Integer> increaseAndCount(Long boardId) {
		return archiveBoardRepository.findById(boardId).map(board -> {
			archiveBoardRepository.increaseLikeCount(boardId);
			return board.getLikeCount() + 1;
		});
	}
}
