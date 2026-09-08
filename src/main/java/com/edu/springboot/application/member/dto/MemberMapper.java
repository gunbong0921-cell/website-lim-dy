package com.edu.springboot.application.member.dto;

import com.edu.springboot.domain.member.Member;

public final class MemberMapper {

	private MemberMapper() {
	}

	public static MemberResponse toResponse(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getLoginId(),
			member.getName(),
			member.getEmail(),
			member.getPhone(),
			member.getCompany(),
			member.isEmailVerified()
		);
	}
}
