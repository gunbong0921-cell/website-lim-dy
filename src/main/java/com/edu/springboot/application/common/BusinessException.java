package com.edu.springboot.application.common;

/**
 * Hexaq
 * 계층: Application
 * 객체: BusinessException
 * 책임: 유스케이스 실패. 핸들러가 message 로 변환
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class BusinessException extends RuntimeException {

	public BusinessException(String message) {
		super(message);
	}
}
