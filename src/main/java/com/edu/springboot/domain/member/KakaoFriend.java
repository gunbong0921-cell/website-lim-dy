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
