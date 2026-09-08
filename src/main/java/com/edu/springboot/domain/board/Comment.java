package com.edu.springboot.domain.board;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {

	private Long id;
	private Long boardId;
	private String writer;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
