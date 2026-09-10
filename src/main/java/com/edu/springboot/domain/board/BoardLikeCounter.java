package com.edu.springboot.domain.board;

import java.util.Optional;

public interface BoardLikeCounter {

	String boardType();

	Optional<Integer> increaseAndCount(Long boardId);
}
