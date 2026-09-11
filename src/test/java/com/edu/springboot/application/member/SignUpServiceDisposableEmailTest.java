package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.application.anomaly.RememberSignupService;
import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.EmailPolicy;
import com.edu.springboot.domain.member.HoneypotPolicy;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: SignUpServiceDisposableEmailTest
 * 책임: 이메일 중복확인이 DB 전에 일회용 도메인을 거절
 * 문서: [docs/security/05-disposable-email.md](../../../../../../../../docs/security/05-disposable-email.md)
 */
@ExtendWith(MockitoExtension.class)
class SignUpServiceDisposableEmailTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private PasswordEncryptor passwordEncryptor;
	@Mock
	private PasswordPolicy passwordPolicy;
	@Mock
	private MailSender mailSender;
	@Mock
	private VerifyBusinessRegistrationService verifyBusinessRegistrationService;
	@Mock
	private VerificationStore phoneVerificationStore;
	@Mock
	private PhoneVerificationPolicy phoneVerificationPolicy;
	@Mock
	private VerifyCaptchaService verifyCaptchaService;
	@Mock
	private RememberSignupService rememberSignupService;

	private SignUpService signUpService;

	@BeforeEach
	void setUp() {
		RejectDisposableEmailService reject = new RejectDisposableEmailService(
			new EmailPolicy(),
			domain -> "mailinator.com".equals(domain)
		);
		signUpService = new SignUpService(
			memberRepository,
			passwordEncryptor,
			passwordPolicy,
			mailSender,
			verifyBusinessRegistrationService,
			phoneVerificationStore,
			phoneVerificationPolicy,
			verifyCaptchaService,
			new HoneypotPolicy(),
			reject,
			rememberSignupService
		);
	}

	@Test
	@DisplayName("중복확인은 일회용 메일이면 DB를 보지 않는다")
	void isEmailAvailable_rejectsDisposableBeforeDb() {
		assertThatThrownBy(() -> signUpService.isEmailAvailable("bot@mailinator.com"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("일회용 이메일은 사용할 수 없습니다.");

		verify(memberRepository, never()).existsByEmail(anyString());
		verify(memberRepository, never()).existsByLoginId(anyString());
	}

	@Test
	@DisplayName("허용 메일은 정규화한 주소로 DB를 본다")
	void isEmailAvailable_queriesNormalizedAddress() {
		when(memberRepository.existsByEmail("user@gmail.com")).thenReturn(false);
		when(memberRepository.existsByLoginId("user@gmail.com")).thenReturn(false);

		assertThat(signUpService.isEmailAvailable("  User@Gmail.COM ")).isTrue();
		verify(memberRepository).existsByEmail("user@gmail.com");
	}
}
