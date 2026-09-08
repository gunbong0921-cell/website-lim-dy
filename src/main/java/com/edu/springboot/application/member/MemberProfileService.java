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
@Transactional
public class MemberProfileService {

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;

	@Transactional(readOnly = true)
	public MemberResponse me(String loginId) {
		return MemberMapper.toResponse(find(loginId));
	}

	public MemberResponse update(String loginId, String name, String phone, String company) {
		if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
			throw new BusinessException("이름과 전화번호는 필수입니다.");
		}
		Member member = find(loginId);
		member.updateProfile(name.trim(), phone.trim(), company == null || company.isBlank() ? null : company.trim());
		return MemberMapper.toResponse(memberRepository.save(member));
	}

	public void changePassword(String loginId, String currentPassword, String newPassword) {
		Member member = find(loginId);
		if (!passwordEncryptor.matches(currentPassword, member.getPassword())) {
			throw new BusinessException("현재 비밀번호가 일치하지 않습니다.");
		}
		if (newPassword == null || newPassword.length() < 8) {
			throw new BusinessException("새 비밀번호는 8자 이상이어야 합니다.");
		}
		member.changePassword(passwordEncryptor.encode(newPassword));
		memberRepository.save(member);
	}

	private Member find(String loginId) {
		return memberRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다."));
	}
}
