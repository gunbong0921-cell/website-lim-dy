package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.SendVerificationResult;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService {

	private final VerificationStore phoneVerificationStore;
	private final PhoneVerificationPolicy phoneVerificationPolicy;
	private final MemberRepository memberRepository;
	private final MailSender mailSender;
	private final VerifyCaptchaService verifyCaptchaService;
	private final RejectDisposableEmailService rejectDisposableEmailService;

	public SendVerificationResult send(String rawEmail, String clientIp, String recaptchaToken, String requestHost) {
		verifyCaptchaService.require(recaptchaToken, CaptchaAction.EMAIL_SEND_CODE, clientIp, requestHost);
		try {
			return doSend(rawEmail, clientIp);
		} catch (BusinessException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw new BusinessException("인증번호 처리에 실패했습니다. 메일 설정을 확인해 주세요.");
		}
	}

	private SendVerificationResult doSend(String rawEmail, String clientIp) {
		String email = rejectDisposableEmailService.requireAllowed(rawEmail);
		if (memberRepository.existsByEmail(email) || memberRepository.existsByLoginId(email)) {
			throw new BusinessException("이미 등록된 이메일입니다.");
		}
		String key = mailKey(email);
		long remaining = phoneVerificationStore.cooldownRemainingSeconds(key);
		if (remaining > 0) {
			throw new BusinessException(remaining + "초 후에 다시 요청할 수 있습니다.");
		}
		assertDailyLimit(phoneVerificationPolicy.dailyPhoneKey(key), "같은 이메일로 하루 발송 한도를 초과했습니다.");
		assertDailyLimit(phoneVerificationPolicy.dailyIpKey(clientIp), "요청이 많아 잠시 후 다시 시도해 주세요.");

		String code = phoneVerificationPolicy.newCode();
		phoneVerificationStore.saveCode(key, code, PhoneVerificationPolicy.CODE_TTL);
		try {
			mailSender.send(email, "[Hexaq] 이메일 인증 코드", phoneVerificationPolicy.messageBody(code));
		} catch (RuntimeException ex) {
			phoneVerificationStore.deleteCode(key);
			throw new BusinessException(ex.getMessage() != null ? ex.getMessage() : "인증번호 발송에 실패했습니다.");
		}
		phoneVerificationStore.startCooldown(key, PhoneVerificationPolicy.COOLDOWN);
		phoneVerificationStore.incrementDaily(
			phoneVerificationPolicy.dailyPhoneKey(key),
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

	static String mailKey(String email) {
		return "MAIL:" + email;
	}
}
