package com.edu.springboot.application.member.dto;

/**
 * Hexaq
 * 계층: Application
 * 객체: MemberResponse
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public record MemberResponse(
	Long id,
	String loginId,
	String name,
	String email,
	String phone,
	String company,
	boolean emailVerified,
	String role,
	String memberType,
	String address,
	String jobTitle,
	String businessNumber,
	String companyName,
	String ceoName,
	String workplaceAddress,
	boolean termsMarketing,
	String oauthProvider,
	String nickname,
	String profileImage,
	java.util.List<KakaoFriendResponse> kakaoFriends,
	boolean kakaoWelcomeSent
) {
	public boolean admin() {
		return "ADMIN".equals(role);
	}

	public boolean corporate() {
		return "CORPORATE".equals(memberType);
	}

	public boolean googleLinked() {
		return "GOOGLE".equals(oauthProvider);
	}

	public boolean githubLinked() {
		return "GITHUB".equals(oauthProvider);
	}

	public boolean kakaoLinked() {
		return "KAKAO".equals(oauthProvider);
	}

	public boolean socialLinked() {
		return googleLinked() || githubLinked() || kakaoLinked();
	}

	public boolean needsPhone() {
		return phone == null || phone.isBlank() || "-".equals(phone.trim());
	}
}
