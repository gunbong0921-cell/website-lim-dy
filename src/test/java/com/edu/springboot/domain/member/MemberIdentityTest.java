package com.edu.springboot.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

/**
 * Hexaq
 * 계층: Test
 * 객체: MemberIdentityTest
 * 책임: 이메일 DI unique, 휴대폰은 DI 아님, 가입 전 소유 확인
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
class MemberIdentityTest {

	@Test
	@DisplayName("email은 unique이고 phone은 unique가 아니다")
	void emailIsDiPhoneIsNot() throws Exception {
		Column email = Member.class.getDeclaredField("email").getAnnotation(Column.class);
		Column phone = Member.class.getDeclaredField("phone").getAnnotation(Column.class);
		Column loginId = Member.class.getDeclaredField("loginId").getAnnotation(Column.class);

		assertThat(email.unique()).isTrue();
		assertThat(email.nullable()).isFalse();
		assertThat(phone.unique()).isFalse();
		assertThat(loginId.unique()).isTrue();
	}

	@Test
	@DisplayName("이메일 또는 휴대폰 중 하나만 확인되면 가입 인증이다")
	void signupVerified_isEmailOrPhone() {
		Member member = new Member();
		assertThat(member.isSignupVerified()).isFalse();
		member.markEmailVerified();
		assertThat(member.isSignupVerified()).isTrue();

		Member phoneOnly = new Member();
		phoneOnly.markPhoneVerified();
		assertThat(phoneOnly.isSignupVerified()).isTrue();
		assertThat(phoneOnly.isEmailVerified()).isFalse();
	}

	@Test
	@DisplayName("채널 기본값은 EMAIL이다")
	void verificationChannel_defaultsToEmail() {
		assertThat(VerificationChannel.from(null)).isEqualTo(VerificationChannel.EMAIL);
		assertThat(VerificationChannel.PHONE.phone()).isTrue();
		assertThat(VerificationChannel.EMAIL.phone()).isFalse();
	}
}
