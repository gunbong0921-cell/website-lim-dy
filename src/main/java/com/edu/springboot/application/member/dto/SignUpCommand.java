package com.edu.springboot.application.member.dto;

public record SignUpCommand(
	String memberType,
	String email,
	String password,
	String passwordConfirm,
	String name,
	String phone,
	String address,
	String jobTitle,
	String businessNumber,
	String companyName,
	String ceoName,
	String workplaceAddress,
	String openingDate,
	Boolean termsService,
	Boolean termsPrivacy,
	Boolean termsMarketing,
	Boolean termsCorporate,
	String verificationChannel,
	String phoneVerificationToken
) {
}
