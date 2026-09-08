package com.edu.springboot.application.member.dto;

import java.util.List;

import com.edu.springboot.domain.member.KakaoFriend;
import com.edu.springboot.domain.member.Member;

public final class MemberMapper {

	private MemberMapper() {
	}

	public static MemberResponse toResponse(Member member) {
		return toResponse(member, List.of());
	}

	public static MemberResponse toResponse(Member member, List<KakaoFriend> friends) {
		List<KakaoFriendResponse> kakaoFriends = friends == null
			? List.of()
			: friends.stream()
				.map(friend -> new KakaoFriendResponse(
					friend.getKakaoId(),
					friend.getNickname(),
					friend.getProfileImage(),
					friend.favorite()
				))
				.toList();
		return new MemberResponse(
			member.getId(),
			member.getLoginId(),
			member.getName(),
			member.getEmail(),
			member.getPhone(),
			member.getCompany(),
			member.isEmailVerified(),
			member.role().name(),
			member.memberType().name(),
			member.getAddress(),
			member.getJobTitle(),
			member.getBusinessNumber(),
			member.getCompanyName(),
			member.getCeoName(),
			member.getWorkplaceAddress(),
			member.marketingAgreed(),
			member.oauthProvider().name(),
			member.getNickname(),
			member.getProfileImage(),
			kakaoFriends,
			member.kakaoWelcomeSent()
		);
	}
}
