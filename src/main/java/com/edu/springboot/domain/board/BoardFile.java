package com.edu.springboot.domain.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardFile {

	private Long id;
	private Long boardId;
	private String originalName;
	private String storedName;
	private String contentType;
	private Long fileSize;
	private String fileType;
}
