package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.MemberMapper;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;

	public MemberResponse authenticate(String loginId, String password) {
		Member member = memberRepository.findByLoginId(loginId)
			.or(() -> memberRepository.findByEmail(loginId))
			.orElseThrow(() -> new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다."));
		if (!passwordEncryptor.matches(password, member.getPassword())) {
			if (member.socialLinked()) {
				throw new BusinessException(member.oauthProvider().displayName()
					+ "로 연동된 계정입니다. 소셜 로그인을 이용하거나 비밀번호 찾기로 비밀번호를 설정하세요.");
			}
			throw new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		if (!member.isSignupVerified()) {
			throw new BusinessException("이메일 또는 휴대폰 인증 후 로그인할 수 있습니다.");
		}
		return MemberMapper.toResponse(member);
	}
}
