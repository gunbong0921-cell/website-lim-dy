package com.edu.springboot.application.member.dto;

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
