package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.MemberMapper;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.domain.member.KakaoFriendRepository;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;
import com.edu.springboot.domain.member.PasswordPolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileService {

	private final MemberRepository memberRepository;
	private final KakaoFriendRepository kakaoFriendRepository;
	private final PasswordEncryptor passwordEncryptor;
	private final PasswordPolicy passwordPolicy;

	@Transactional(readOnly = true)
	public MemberResponse me(String loginId) {
		return mapped(find(loginId));
	}

	public MemberResponse update(String loginId, String name, String phone, String company, String address,
		String jobTitle, String workplaceAddress) {
		if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
			throw new BusinessException("이름과 전화번호는 필수입니다.");
		}
		String digits = phone.replaceAll("\\D", "");
		if (digits.length() < 10 || digits.length() > 11) {
			throw new BusinessException("휴대폰 번호는 하이픈 없이 숫자 10~11자리로 입력하세요.");
		}
		Member member = find(loginId);
		member.updateProfile(
			name.trim(),
			digits,
			company == null || company.isBlank() ? null : company.trim(),
			address == null || address.isBlank() ? null : address.trim(),
			jobTitle == null || jobTitle.isBlank() ? null : jobTitle.trim(),
			workplaceAddress == null || workplaceAddress.isBlank() ? null : workplaceAddress.trim()
		);
		return mapped(memberRepository.save(member));
	}

	public void changePassword(String loginId, String currentPassword, String newPassword) {
		Member member = find(loginId);
		if (!passwordEncryptor.matches(currentPassword, member.getPassword())) {
			throw new BusinessException("현재 비밀번호가 일치하지 않습니다.");
		}
		if (!passwordPolicy.matches(newPassword)) {
			throw new BusinessException(passwordPolicy.ruleMessage());
		}
		member.changePassword(passwordEncryptor.encode(newPassword));
		memberRepository.save(member);
	}

	private MemberResponse mapped(Member member) {
		return MemberMapper.toResponse(member, kakaoFriendRepository.findByMemberId(member.getId()));
	}

	private Member find(String loginId) {
		return memberRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다."));
	}
}
