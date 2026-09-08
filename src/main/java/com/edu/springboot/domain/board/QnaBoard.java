package com.edu.springboot.domain.board;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaBoard {

	private Long id;
	private String title;
	private String content;
	private String writer;
	private String solution;
	private int visitCount;
	private int likeCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
