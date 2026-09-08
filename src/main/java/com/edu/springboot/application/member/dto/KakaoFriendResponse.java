package com.edu.springboot.application.member.dto;

public record KakaoFriendResponse(
	String kakaoId,
	String nickname,
	String profileImage,
	boolean favorite
) {
}
