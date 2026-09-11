package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.sms.SmsSender;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryPhoneVerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: SendPhoneVerificationServiceTest
 * 책임: 캡차 뒤 형식·쿨다운·일 10회, SMS 전 거절
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
@ExtendWith(MockitoExtension.class)
class SendPhoneVerificationServiceTest {

	@Mock
	private SmsSender smsSender;
	@Mock
	private VerifyCaptchaService verifyCaptchaService;

	private final InMemoryPhoneVerificationStore store = new InMemoryPhoneVerificationStore();
	private final PhoneVerificationPolicy policy = new PhoneVerificationPolicy();
	private SendPhoneVerificationService service;

	@BeforeEach
	void setUp() {
		service = new SendPhoneVerificationService(store, policy, smsSender, verifyCaptchaService);
	}

	@Test
	@DisplayName("잘못된 번호면 SMS를 보내지 않는다")
	void send_rejectsInvalidMobileBeforeSms() {
		assertThatThrownBy(() -> service.send("02-123-4567", "1.1.1.1", "tok", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("휴대폰 번호는 하이픈 없이 숫자 10~11자리로 입력하세요.");

		verify(smsSender, never()).send(anyString(), anyString());
	}

	@Test
	@DisplayName("쿨다운 중이면 SMS를 보내지 않는다")
	void send_rejectsDuringCooldown() {
		store.startCooldown("01012345678", PhoneVerificationPolicy.COOLDOWN);

		assertThatThrownBy(() -> service.send("01012345678", "1.1.1.1", "tok", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("초 후에 다시 요청할 수 있습니다.");

		verify(smsSender, never()).send(anyString(), anyString());
	}

	@Test
	@DisplayName("같은 번호 일 10회면 SMS를 보내지 않는다")
	void send_rejectsDailyLimitBeforeSms() {
		String bucket = policy.dailyPhoneKey("01012345678");
		for (int i = 0; i < PhoneVerificationPolicy.DAILY_LIMIT; i++) {
			store.incrementDaily(bucket, PhoneVerificationPolicy.CODE_TTL);
		}

		assertThatThrownBy(() -> service.send("01012345678", "1.1.1.1", "tok", "localhost"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("같은 번호로 하루 발송 한도를 초과했습니다.");

		verify(smsSender, never()).send(anyString(), anyString());
	}

	@Test
	@DisplayName("SMS 실패면 저장한 코드를 지운다")
	void send_deletesCodeWhenSmsFails() {
		doThrow(new RuntimeException("fail")).when(smsSender).send(anyString(), anyString());

		assertThatThrownBy(() -> service.send("01012345678", "1.1.1.1", "tok", "localhost"))
			.isInstanceOf(BusinessException.class);

		assertThat(store.findCode("01012345678")).isEmpty();
	}
}
