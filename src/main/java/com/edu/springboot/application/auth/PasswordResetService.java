package com.edu.springboot.application.auth;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;
	private final MailSender mailSender;

	public PasswordResetResult sendTemporaryPassword(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException("해당 이메일로 가입된 계정이 없습니다."));
		String temp = generate(10);
		member.changePassword(passwordEncryptor.encode(temp));
		memberRepository.save(member);
		try {
			mailSender.send(email, "[Hexaq] 임시 비밀번호",
				"임시 비밀번호는 " + temp + " 입니다. 로그인 후 반드시 비밀번호를 변경하세요.");
			return new PasswordResetResult(true, null);
		} catch (Exception ex) {
			return new PasswordResetResult(false, temp);
		}
	}

	public record PasswordResetResult(boolean mailSent, String debugPassword) {
	}

	private String generate(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}
}
