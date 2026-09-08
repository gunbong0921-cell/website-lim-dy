package com.edu.springboot.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindLoginIdService {

	private final MemberRepository memberRepository;
	private final MailSender mailSender;

	public FindLoginIdResult sendLoginId(String email) {
		if (email == null || email.isBlank()) {
			throw new BusinessException("이메일을 입력하세요.");
		}
		Member member = memberRepository.findByEmail(email.trim())
			.orElseThrow(() -> new BusinessException("해당 이메일로 가입된 계정이 없습니다."));
		try {
			mailSender.send(member.getEmail(), "[Hexaq] 아이디 안내",
				"가입하신 아이디는 " + member.getLoginId() + " 입니다.");
			return new FindLoginIdResult(true, null);
		} catch (Exception ex) {
			return new FindLoginIdResult(false, member.getLoginId());
		}
	}

	public record FindLoginIdResult(boolean mailSent, String debugLoginId) {
	}
}
