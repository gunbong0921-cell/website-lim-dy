package com.edu.springboot.application.member.dto;

public record BusinessVerifyCommand(
	String businessNumber,
	String openingDate,
	String representativeName,
	String companyName,
	String address
) {
}
