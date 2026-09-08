package com.edu.springboot.application.common;

import java.util.List;

public record PageResponse<T>(
	List<T> content,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
	public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
		int totalPages = (int) Math.ceil(totalElements / (double) size);
		if (totalPages == 0) {
			totalPages = 1;
		}
		return new PageResponse<>(content, page, size, totalElements, totalPages);
	}
}
