package com.edu.springboot.domain.member;

public record BusinessRegistrationResult(
	boolean matched,
	boolean operating,
	String statusName,
	String taxType,
	String message
) {
}
