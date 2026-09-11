package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;

/**
 * Hexaq
 * 계층: Test
 * 객체: LoginServiceIdentityTest
 * 책임: 미인증 계정 로그인 거절
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceIdentityTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private PasswordEncryptor passwordEncryptor;
	@Mock
	private VerifyCaptchaService verifyCaptchaService;

	@Test
	@DisplayName("이메일·휴대폰 모두 미확인이면 로그인할 수 없다")
	void authenticate_rejectsUnverifiedMember() {
		Member member = new Member();
		member.setLoginId("user@gmail.com");
		member.setPassword("enc");
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		when(passwordEncryptor.matches("pw", "enc")).thenReturn(true);
		LoginService loginService = new LoginService(memberRepository, passwordEncryptor, verifyCaptchaService);

		assertThatThrownBy(() -> loginService.authenticate("user@gmail.com", "pw", "tok", "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("이메일 또는 휴대폰 인증 후 로그인할 수 있습니다.");
	}

	@Test
	@DisplayName("휴대폰 확인이면 로그인할 수 있다")
	void authenticate_allowsPhoneVerifiedMember() {
		Member member = new Member();
		member.setLoginId("user@gmail.com");
		member.setPassword("enc");
		member.markPhoneVerified();
		when(memberRepository.findByLoginId("user@gmail.com")).thenReturn(Optional.of(member));
		when(passwordEncryptor.matches("pw", "enc")).thenReturn(true);
		LoginService loginService = new LoginService(memberRepository, passwordEncryptor, verifyCaptchaService);

		loginService.authenticate("user@gmail.com", "pw", "tok", "1.1.1.1", "localhost");
	}
}
