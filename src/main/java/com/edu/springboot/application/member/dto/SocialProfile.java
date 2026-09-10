package com.edu.springboot.application.member.dto;

import java.util.List;

/**
 * Hexaq
 * 계층: Application
 * 객체: SocialProfile
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public record SocialProfile(
	String provider,
	String subject,
	String email,
	String name,
	String profileImage,
	List<SocialFriend> friends,
	String accessToken
) {
	public SocialProfile(String provider, String subject, String email, String name) {
		this(provider, subject, email, name, "", List.of(), "");
	}

	public SocialProfile {
		profileImage = profileImage == null ? "" : profileImage;
		friends = friends == null ? List.of() : List.copyOf(friends);
		accessToken = accessToken == null ? "" : accessToken;
	}

	public record SocialFriend(
		String id,
		String nickname,
		String profileImage,
		boolean favorite
	) {
	}
}
