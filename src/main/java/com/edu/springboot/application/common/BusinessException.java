package com.edu.springboot.application.common;

public class BusinessException extends RuntimeException {

	public BusinessException(String message) {
		super(message);
	}
}
