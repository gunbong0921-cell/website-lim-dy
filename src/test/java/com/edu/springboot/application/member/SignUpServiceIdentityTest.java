package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

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
import com.edu.springboot.domain.member.EmailPolicy;
import com.edu.springboot.domain.member.HoneypotPolicy;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryPhoneVerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: SignUpServiceIdentityTest
 * 책임: 같은 이메일 DI 거절, 가입 시 토큰 1회 소비
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
@ExtendWith(MockitoExtension.class)
class SignUpServiceIdentityTest {

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
	private VerifyCaptchaService verifyCaptchaService;
	@Mock
	private RememberSignupService rememberSignupService;

	private final InMemoryPhoneVerificationStore store = new InMemoryPhoneVerificationStore();
	private SignUpService signUpService;

	@BeforeEach
	void setUp() {
		signUpService = new SignUpService(
			memberRepository,
			passwordEncryptor,
			passwordPolicy,
			mailSender,
			verifyBusinessRegistrationService,
			store,
			new PhoneVerificationPolicy(),
			verifyCaptchaService,
			new HoneypotPolicy(),
			new RejectDisposableEmailService(new EmailPolicy(), domain -> false),
			rememberSignupService
		);
	}

	@Test
	@DisplayName("이미 있는 이메일이면 토큰을 소비하지 않는다")
	void signUp_rejectsDuplicateEmailBeforeTokenConsume() {
		store.saveToken("tok", "01012345678", Duration.ofMinutes(30));
		when(passwordPolicy.matches(any())).thenReturn(true);
		when(memberRepository.existsByEmail("user@gmail.com")).thenReturn(true);

		assertThatThrownBy(() -> signUpService.signUp(command("tok"), "cap", "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("이미 등록된 이메일입니다.");

		assertThat(store.consumeToken("tok")).contains("01012345678");
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("토큰이 없으면 가입하지 않는다")
	void signUp_requiresVerificationToken() {
		when(passwordPolicy.matches(any())).thenReturn(true);

		assertThatThrownBy(() -> signUpService.signUp(command(null), "cap", "1.1.1.1", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("본인 인증을 완료해 주세요.");
	}

	@Test
	@DisplayName("휴대폰 토큰을 한 번 쓰고 가입하면 phoneVerified다")
	void signUp_consumesPhoneTokenOnce() {
		store.saveToken("tok", "01012345678", Duration.ofMinutes(30));
		when(passwordPolicy.matches(any())).thenReturn(true);
		when(passwordEncryptor.encode(any())).thenReturn("enc");
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

		signUpService.signUp(command("tok"), "cap", "1.1.1.1", "localhost");

		verify(memberRepository).save(org.mockito.ArgumentMatchers.argThat(Member::isPhoneVerified));
		assertThat(store.consumeToken("tok")).isEmpty();
	}

	private SignUpCommand command(String token) {
		return new SignUpCommand(
			"INDIVIDUAL",
			"user@gmail.com",
			"Abcd1234!",
			"Abcd1234!",
			"홍길동",
			"01012345678",
			"서울",
			null, null, null, null, null, null,
			true, true, false, false,
			"PHONE",
			token,
			""
		);
	}
}
