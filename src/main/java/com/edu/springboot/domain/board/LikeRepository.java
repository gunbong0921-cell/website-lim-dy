package com.edu.springboot.domain.board;

public interface LikeRepository {

	boolean exists(String boardType, Long boardId, String memberLoginId);

	void insert(String boardType, Long boardId, String memberLoginId);
}
