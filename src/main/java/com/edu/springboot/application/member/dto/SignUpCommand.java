package com.edu.springboot.application.member.dto;

public record SignUpCommand(
	String loginId,
	String password,
	String passwordConfirm,
	String name,
	String email,
	String phone,
	String company
) {
}
