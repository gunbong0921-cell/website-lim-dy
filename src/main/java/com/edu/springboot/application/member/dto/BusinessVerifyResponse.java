package com.edu.springboot.application.member.dto;

public record BusinessVerifyResponse(
	boolean matched,
	boolean operating,
	String statusName,
	String taxType
) {
}
