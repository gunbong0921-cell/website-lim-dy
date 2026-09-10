package com.edu.springboot.application.common;

import java.util.List;

/**
 * Hexaq
 * 계층: Application
 * 객체: PageResponse
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
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
