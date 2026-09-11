package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.application.anomaly.RememberSignupService;
import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.SignUpCommand;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.HoneypotPolicy;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: SignUpServiceHoneypotTest
 * 책임: 가입 website가 있으면 캡차·DB 전에 거절
 * 문서: [docs/security/04-honeypot-field.md](../../../../../../../../docs/security/04-honeypot-field.md)
 */
@ExtendWith(MockitoExtension.class)
class SignUpServiceHoneypotTest {

	private static final String FAIL = "요청을 처리할 수 없습니다.";

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
	private RejectDisposableEmailService rejectDisposableEmailService;
	@Mock
	private RememberSignupService rememberSignupService;

	private SignUpService signUpService;

	@BeforeEach
	void setUp() {
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
			rejectDisposableEmailService,
			rememberSignupService
		);
	}

	@Test
	@DisplayName("website에 값이 있으면 캡차와 DB 없이 거절한다")
	void signUp_rejectsFilledHoneypotBeforeCaptchaAndDb() {
		assertThatThrownBy(() -> signUpService.signUp(command("https://spam.example"), "token", "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage(FAIL);

		verify(verifyCaptchaService, never()).require(any(), any(), any(), any());
		verify(memberRepository, never()).save(any());
		verify(rejectDisposableEmailService, never()).requireAllowed(anyString());
	}

	@Test
	@DisplayName("website가 비면 허니팟을 건너뛰고 다음 검사를 한다")
	void signUp_skipsHoneypotWhenBlank() {
		assertThatThrownBy(() -> signUpService.signUp(command("  "), "token", "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("필수 항목을 모두 입력하세요.");

		verify(verifyCaptchaService).require(any(), any(), any(), any());
		verify(memberRepository, never()).save(any());
	}

	private SignUpCommand command(String website) {
		return new SignUpCommand(
			null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, website
		);
	}
}
