package com.edu.springboot.presentation.dto;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: ApiResponse
 * 책임: JSON 봉투 { success, data, message }
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md)
 */
public record ApiResponse<T>(boolean success, T data, String message) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> ok(T data, String message) {
		return new ApiResponse<>(true, data, message);
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, null, message);
	}
}
