package com.edu.springboot.application.member;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.VerifyPhoneCodeResult;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.PhoneVerificationStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyPhoneCodeService {

	private final PhoneVerificationStore phoneVerificationStore;
	private final PhoneVerificationPolicy phoneVerificationPolicy;

	public VerifyPhoneCodeResult verify(String rawPhone, String code) {
		String phone = phoneVerificationPolicy.normalize(rawPhone);
		if (!phoneVerificationPolicy.validMobile(phone)) {
			throw new BusinessException("휴대폰 번호는 하이픈 없이 숫자 10~11자리로 입력하세요.");
		}
		if (code == null || !code.trim().matches("\\d{6}")) {
			throw new BusinessException("인증번호 6자리를 입력하세요.");
		}
		String saved = phoneVerificationStore.findCode(phone)
			.orElseThrow(() -> new BusinessException("인증번호가 만료되었거나 발송되지 않았습니다."));
		if (!saved.equals(code.trim())) {
			throw new BusinessException("인증번호가 올바르지 않습니다.");
		}
		phoneVerificationStore.deleteCode(phone);
		String token = UUID.randomUUID().toString().replace("-", "");
		phoneVerificationStore.saveToken(token, phone, PhoneVerificationPolicy.TOKEN_TTL);
		return new VerifyPhoneCodeResult(token, phone);
	}
}
