package com.edu.springboot.application.member;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.MemberMapper;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.application.member.dto.SignUpCommand;
import com.edu.springboot.application.member.dto.SignUpResult;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;
	private final MailSender mailSender;

	public boolean isLoginIdAvailable(String loginId) {
		return loginId != null && !loginId.isBlank() && !memberRepository.existsByLoginId(loginId);
	}

	public boolean isEmailAvailable(String email) {
		return email != null && !email.isBlank() && !memberRepository.existsByEmail(email);
	}

	public SignUpResult signUp(SignUpCommand command) {
		validate(command);
		if (memberRepository.existsByLoginId(command.loginId())) {
			throw new BusinessException("이미 사용 중인 아이디입니다.");
		}
		if (memberRepository.existsByEmail(command.email())) {
			throw new BusinessException("이미 등록된 이메일입니다.");
		}

		String code = newCode();
		Member member = new Member();
		member.setLoginId(command.loginId().trim());
		member.setPassword(passwordEncryptor.encode(command.password()));
		member.setName(command.name().trim());
		member.setEmail(command.email().trim());
		member.setPhone(command.phone().trim());
		member.setCompany(blankToNull(command.company()));
		member.setEmailVerified("N");
		member.setVerificationCode(code);
		member.setVerificationExpires(LocalDateTime.now().plusMinutes(30));
		memberRepository.save(member);

		boolean mailSent = sendVerificationMail(member.getEmail(), code);
		return new SignUpResult(member.getId(), mailSent, mailSent ? null : code);
	}

	public SignUpResult resendVerification(String loginId) {
		Member member = memberRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다."));
		if (member.isEmailVerified()) {
			throw new BusinessException("이미 이메일 인증이 완료된 계정입니다.");
		}
		String code = newCode();
		member.setVerificationCode(code);
		member.setVerificationExpires(LocalDateTime.now().plusMinutes(30));
		memberRepository.save(member);
		boolean mailSent = sendVerificationMail(member.getEmail(), code);
		return new SignUpResult(member.getId(), mailSent, mailSent ? null : code);
	}

	public MemberResponse verifyEmail(String loginId, String code) {
		Member member = memberRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다."));
		if (member.isEmailVerified()) {
			return MemberMapper.toResponse(member);
		}
		if (!member.matchesVerification(code, LocalDateTime.now())) {
			throw new BusinessException("인증 코드가 올바르지 않거나 만료되었습니다.");
		}
		member.markEmailVerified();
		return MemberMapper.toResponse(memberRepository.save(member));
	}

	private void validate(SignUpCommand command) {
		if (isBlank(command.loginId()) || isBlank(command.password()) || isBlank(command.name())
			|| isBlank(command.email()) || isBlank(command.phone())) {
			throw new BusinessException("필수 항목을 모두 입력하세요.");
		}
		if (!command.loginId().matches("^[a-zA-Z0-9_]{4,20}$")) {
			throw new BusinessException("아이디는 영문·숫자·밑줄 4~20자여야 합니다.");
		}
		if (!command.email().contains("@") || command.email().length() > 100) {
			throw new BusinessException("이메일 형식이 올바르지 않습니다.");
		}
		if (!command.password().equals(command.passwordConfirm())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.");
		}
		if (command.password().length() < 8) {
			throw new BusinessException("비밀번호는 8자 이상이어야 합니다.");
		}
	}

	private String newCode() {
		return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
	}

	private String blankToNull(String value) {
		return isBlank(value) ? null : value.trim();
	}

	private boolean sendVerificationMail(String email, String code) {
		try {
			mailSender.send(email, "[Hexaq] 이메일 인증 코드",
				"Hexaq 회원가입 인증 코드는 " + code + " 입니다. 30분 안에 입력해 주세요.");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
