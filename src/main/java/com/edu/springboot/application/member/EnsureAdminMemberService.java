package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.MemberRole;
import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: EnsureAdminMemberService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnsureAdminMemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;

	public void ensure(String loginId, String password, String name, String email, String phone) {
		if (isBlank(loginId) || isBlank(password)) {
			return;
		}
		memberRepository.findByLoginId(loginId.trim()).ifPresentOrElse(
			member -> promote(member, password),
			() -> create(loginId.trim(), password, name, email, phone)
		);
	}

	private void promote(Member member, String password) {
		member.changePassword(passwordEncryptor.encode(password));
		member.assignRole(MemberRole.ADMIN);
		member.markEmailVerified();
		memberRepository.save(member);
	}

	private void create(String loginId, String password, String name, String email, String phone) {
		Member member = new Member();
		member.setLoginId(loginId);
		member.setPassword(passwordEncryptor.encode(password));
		member.setName(isBlank(name) ? "관리자" : name.trim());
		member.setEmail(uniqueEmail(loginId, email));
		member.setPhone(isBlank(phone) ? "010-0000-0000" : phone.trim());
		member.setCompany("Hexaq");
		member.assignRole(MemberRole.ADMIN);
		member.markEmailVerified();
		memberRepository.save(member);
	}

	private String uniqueEmail(String loginId, String email) {
		String candidate = isBlank(email) ? loginId + "@hexaq.local" : email.trim();
		if (!memberRepository.existsByEmail(candidate)) {
			return candidate;
		}
		return loginId + ".admin@hexaq.local";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
