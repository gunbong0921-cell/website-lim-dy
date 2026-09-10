package com.edu.springboot.application.member.dto;

import java.util.List;

import com.edu.springboot.domain.member.KakaoFriend;
import com.edu.springboot.domain.member.Member;

/**
 * Hexaq
 * 계층: Application
 * 객체: MemberMapper
 * 책임: Member → MemberResponse 변환
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
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
