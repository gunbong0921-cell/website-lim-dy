package com.edu.springboot.domain.member;

public interface KakaoTalkGateway {

	boolean sendWelcome(String accessToken, String memberName);
}
