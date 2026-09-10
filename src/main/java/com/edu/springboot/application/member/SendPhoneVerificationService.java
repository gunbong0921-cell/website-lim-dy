package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.SendVerificationResult;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;
import com.edu.springboot.domain.sms.SmsSender;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: SendPhoneVerificationService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
public class SendPhoneVerificationService {

	private final VerificationStore phoneVerificationStore;
	private final PhoneVerificationPolicy phoneVerificationPolicy;
	private final SmsSender smsSender;
	private final VerifyCaptchaService verifyCaptchaService;

	public SendVerificationResult send(String rawPhone, String clientIp, String recaptchaToken, String requestHost) {
		verifyCaptchaService.require(recaptchaToken, CaptchaAction.PHONE_SEND_CODE, clientIp, requestHost);
		try {
			return doSend(rawPhone, clientIp);
		} catch (BusinessException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw new BusinessException("인증번호 처리에 실패했습니다. Redis 또는 SMS 설정을 확인해 주세요.");
		}
	}

	private SendVerificationResult doSend(String rawPhone, String clientIp) {
		String phone = phoneVerificationPolicy.normalize(rawPhone);
		if (!phoneVerificationPolicy.validMobile(phone)) {
			throw new BusinessException("휴대폰 번호는 하이픈 없이 숫자 10~11자리로 입력하세요.");
		}
		long remaining = phoneVerificationStore.cooldownRemainingSeconds(phone);
		if (remaining > 0) {
			throw new BusinessException(remaining + "초 후에 다시 요청할 수 있습니다.");
		}
		assertDailyLimit(phoneVerificationPolicy.dailyPhoneKey(phone), "같은 번호로 하루 발송 한도를 초과했습니다.");
		assertDailyLimit(phoneVerificationPolicy.dailyIpKey(clientIp), "요청이 많아 잠시 후 다시 시도해 주세요.");

		String code = phoneVerificationPolicy.newCode();
		phoneVerificationStore.saveCode(phone, code, PhoneVerificationPolicy.CODE_TTL);
		try {
			smsSender.send(phone, phoneVerificationPolicy.messageBody(code));
		} catch (RuntimeException ex) {
			phoneVerificationStore.deleteCode(phone);
			throw new BusinessException(ex.getMessage() != null ? ex.getMessage() : "인증번호 발송에 실패했습니다.");
		}
		phoneVerificationStore.startCooldown(phone, PhoneVerificationPolicy.COOLDOWN);
		phoneVerificationStore.incrementDaily(
			phoneVerificationPolicy.dailyPhoneKey(phone),
			phoneVerificationPolicy.ttlUntilMidnight()
		);
		phoneVerificationStore.incrementDaily(
			phoneVerificationPolicy.dailyIpKey(clientIp),
			phoneVerificationPolicy.ttlUntilMidnight()
		);
		return new SendVerificationResult(
			(int) PhoneVerificationPolicy.COOLDOWN.toSeconds(),
			(int) PhoneVerificationPolicy.CODE_TTL.toSeconds()
		);
	}

	private void assertDailyLimit(String bucketKey, String message) {
		if (phoneVerificationStore.dailyCount(bucketKey) >= PhoneVerificationPolicy.DAILY_LIMIT) {
			throw new BusinessException(message);
		}
	}
}
