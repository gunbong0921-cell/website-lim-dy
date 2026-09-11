package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.EmailPolicy;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: SendEmailVerificationServiceDisposableEmailTest
 * 책임: 인증 메일 발송이 DB·SMTP 전에 일회용 도메인을 거절
 * 문서: [docs/security/05-disposable-email.md](../../../../../../../../docs/security/05-disposable-email.md)
 */
@ExtendWith(MockitoExtension.class)
class SendEmailVerificationServiceDisposableEmailTest {

	@Mock
	private VerificationStore phoneVerificationStore;
	@Mock
	private PhoneVerificationPolicy phoneVerificationPolicy;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MailSender mailSender;
	@Mock
	private VerifyCaptchaService verifyCaptchaService;

	private SendEmailVerificationService sendEmailVerificationService;

	@BeforeEach
	void setUp() {
		sendEmailVerificationService = new SendEmailVerificationService(
			phoneVerificationStore,
			phoneVerificationPolicy,
			memberRepository,
			mailSender,
			verifyCaptchaService,
			new RejectDisposableEmailService(new EmailPolicy(), domain -> "mailinator.com".equals(domain))
		);
	}

	@Test
	@DisplayName("일회용 메일이면 회원 조회와 SMTP를 하지 않는다")
	void send_rejectsDisposableBeforeDbAndMail() {
		assertThatThrownBy(() -> sendEmailVerificationService.send(
			"bot@mailinator.com", "1.1.1.1", "token", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("일회용 이메일은 사용할 수 없습니다.");

		verify(memberRepository, never()).existsByEmail(anyString());
		verify(mailSender, never()).send(anyString(), anyString(), anyString());
	}
}
