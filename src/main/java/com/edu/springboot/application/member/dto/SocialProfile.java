package com.edu.springboot.application.member.dto;

import java.util.List;

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
