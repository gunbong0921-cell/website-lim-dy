package com.edu.springboot.application.member;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.VerifyCodeResult;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyEmailCodeService {

	private final VerificationStore phoneVerificationStore;
	private final PhoneVerificationPolicy phoneVerificationPolicy;

	public VerifyCodeResult verify(String rawEmail, String code) {
		String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
		if (!email.contains("@") || email.length() > 100) {
			throw new BusinessException("이메일 형식이 올바르지 않습니다.");
		}
		if (code == null || !code.trim().matches("\\d{6}")) {
			throw new BusinessException("인증번호 6자리를 입력하세요.");
		}
		String key = SendEmailVerificationService.mailKey(email);
		String saved = phoneVerificationStore.findCode(key)
			.orElseThrow(() -> new BusinessException("인증번호가 만료되었거나 발송되지 않았습니다."));
		if (!saved.equals(code.trim())) {
			throw new BusinessException("인증번호가 올바르지 않습니다.");
		}
		phoneVerificationStore.deleteCode(key);
		String token = UUID.randomUUID().toString().replace("-", "");
		phoneVerificationStore.saveToken(token, key, PhoneVerificationPolicy.TOKEN_TTL);
		return new VerifyCodeResult(token, email);
	}
}
