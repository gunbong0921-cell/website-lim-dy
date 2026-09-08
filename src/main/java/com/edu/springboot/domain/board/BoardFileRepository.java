package com.edu.springboot.domain.board;

import java.util.List;

public interface BoardFileRepository {

	void save(BoardFile file);

	List<BoardFile> findByBoardId(Long boardId);

	java.util.Optional<BoardFile> findById(Long id);

	void deleteByBoardId(Long boardId);
}
