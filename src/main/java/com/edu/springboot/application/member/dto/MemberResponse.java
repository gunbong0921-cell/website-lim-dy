package com.edu.springboot.application.member.dto;

public record MemberResponse(
	Long id,
	String loginId,
	String name,
	String email,
	String phone,
	String company,
	boolean emailVerified
) {
}
