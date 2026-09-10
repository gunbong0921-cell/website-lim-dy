package com.edu.springboot.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hexaq
 * 계층: Domain
 * 객체: KakaoFriend
 * 책임: KakaoFriend 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Entity
@Table(
	name = "HEXAQ_KAKAO_FRIEND",
	uniqueConstraints = @UniqueConstraint(name = "uk_kakao_friend", columnNames = { "member_id", "kakao_id" })
)
@Getter
@Setter
@NoArgsConstructor
public class KakaoFriend {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "kakao_id", nullable = false, length = 100)
	private String kakaoId;

	@Column(length = 100)
	private String nickname;

	@Column(name = "profile_image", length = 1000)
	private String profileImage;

	@Column(length = 1)
	private String favorite = "N";

	public static KakaoFriend of(Long memberId, String kakaoId, String nickname, String profileImage, boolean favorite) {
		KakaoFriend friend = new KakaoFriend();
		friend.memberId = memberId;
		friend.kakaoId = kakaoId;
		friend.nickname = nickname;
		friend.profileImage = profileImage;
		friend.favorite = favorite ? "Y" : "N";
		return friend;
	}

	public boolean favorite() {
		return "Y".equals(favorite);
	}
}
