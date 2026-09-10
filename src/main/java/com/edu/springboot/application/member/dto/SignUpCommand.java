package com.edu.springboot.application.member.dto;

/**
 * Hexaq
 * 계층: Application
 * 객체: SignUpCommand
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
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
	String phoneVerificationToken,
	String website
) {
}
