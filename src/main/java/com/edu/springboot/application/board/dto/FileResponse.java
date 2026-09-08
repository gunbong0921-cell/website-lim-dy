package com.edu.springboot.application.board.dto;

public record FileResponse(
	Long id,
	String originalName,
	String url,
	String fileType,
	Long fileSize
) {
}
