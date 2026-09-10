package com.edu.springboot.application.member;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.anomaly.RememberSignupService;
import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.BusinessVerifyCommand;
import com.edu.springboot.application.member.dto.MemberMapper;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.application.member.dto.SignUpCommand;
import com.edu.springboot.application.member.dto.SignUpResult;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.mail.MailSender;
import com.edu.springboot.domain.member.HoneypotPolicy;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.MemberRole;
import com.edu.springboot.domain.member.MemberType;
import com.edu.springboot.domain.member.PasswordEncryptor;
import com.edu.springboot.domain.member.PasswordPolicy;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.domain.member.VerificationStore;
import com.edu.springboot.domain.member.VerificationChannel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

	private final MemberRepository memberRepository;
	private final PasswordEncryptor passwordEncryptor;
	private final PasswordPolicy passwordPolicy;
	private final MailSender mailSender;
	private final VerifyBusinessRegistrationService verifyBusinessRegistrationService;
	private final VerificationStore phoneVerificationStore;
	private final PhoneVerificationPolicy phoneVerificationPolicy;
	private final VerifyCaptchaService verifyCaptchaService;
	private final HoneypotPolicy honeypotPolicy;
	private final RejectDisposableEmailService rejectDisposableEmailService;
	private final RememberSignupService rememberSignupService;

	public boolean isLoginIdAvailable(String loginId) {
		if (loginId == null || loginId.isBlank()) {
			throw new BusinessException("아이디를 입력한 뒤 중복확인 해주세요.");
		}
		return !memberRepository.existsByLoginId(loginId.trim()) && !memberRepository.existsByEmail(loginId.trim());
	}

	public boolean isEmailAvailable(String email) {
		String value = rejectDisposableEmailService.requireAllowed(email);
		return !memberRepository.existsByEmail(value) && !memberRepository.existsByLoginId(value);
	}

	public SignUpResult signUp(SignUpCommand command, String recaptchaToken, String clientIp, String requestHost) {
		if (honeypotPolicy.tripped(command.website())) {
			throw new BusinessException("요청을 처리할 수 없습니다.");
		}
		verifyCaptchaService.require(recaptchaToken, CaptchaAction.SIGNUP, clientIp, requestHost);
		MemberType type = MemberType.from(command.memberType());
		VerificationChannel channel = VerificationChannel.from(command.verificationChannel());
		validate(command, type);
		String email = rejectDisposableEmailService.requireAllowed(command.email());
		if (memberRepository.existsByLoginId(email) || memberRepository.existsByEmail(email)) {
			throw new BusinessException("이미 등록된 이메일입니다.");
		}

		String phone = digits(command.phone());
		if (channel.phone()) {
			consumePhoneToken(command.phoneVerificationToken(), phone);
		} else {
			consumePhoneToken(command.phoneVerificationToken(), SendEmailVerificationService.mailKey(email.toLowerCase()));
		}
		Member member = new Member();
		member.setLoginId(email);
		member.setPassword(passwordEncryptor.encode(command.password()));
		member.setName(command.name().trim());
		member.setEmail(email);
		member.setPhone(phone);
		member.assignType(type);
		member.assignRole(MemberRole.USER);
		member.assignVerificationChannel(channel);
		if (channel.phone()) {
			member.setEmailVerified("N");
			member.markPhoneVerified();
		} else {
			member.markEmailVerified();
		}
		member.agreeTerms(
			Boolean.TRUE.equals(command.termsService()),
			Boolean.TRUE.equals(command.termsPrivacy()),
			Boolean.TRUE.equals(command.termsMarketing()),
			Boolean.TRUE.equals(command.termsCorporate())
		);
		if (type.corporate()) {
			String companyName = command.companyName().trim();
			member.setCompanyName(companyName);
			member.setCompany(companyName);
			member.setBusinessNumber(digits(command.businessNumber()));
			member.setCeoName(command.ceoName().trim());
			member.setJobTitle(command.jobTitle().trim());
			member.setWorkplaceAddress(blankToNull(command.workplaceAddress()));
		} else {
			member.setAddress(command.address().trim());
			member.setJobTitle(blankToNull(command.jobTitle()));
			member.setCompany(blankToNull(command.jobTitle()));
		}
		memberRepository.save(member);
		rememberSignupService.remember(member.getLoginId());
		return new SignUpResult(member.getId(), true, null, channel.name());
	}

	public SignUpResult resendVerification(String loginId) {
		Member member = findByLoginOrEmail(loginId);
		if (member.isEmailVerified()) {
			throw new BusinessException("이미 이메일 인증이 완료된 계정입니다.");
		}
		if (member.isPhoneVerified()) {
			throw new BusinessException("휴대폰 인증으로 가입한 계정입니다.");
		}
		String code = newCode();
		member.setVerificationCode(code);
		member.setVerificationExpires(LocalDateTime.now().plusMinutes(30));
		memberRepository.save(member);
		boolean mailSent = sendVerificationMail(member.getEmail(), code);
		return new SignUpResult(member.getId(), mailSent, mailSent ? null : code, VerificationChannel.EMAIL.name());
	}

	public MemberResponse verifyEmail(String loginId, String code) {
		Member member = findByLoginOrEmail(loginId);
		if (member.isEmailVerified()) {
			return MemberMapper.toResponse(member);
		}
		if (!member.matchesVerification(code, LocalDateTime.now())) {
			throw new BusinessException("인증 코드가 올바르지 않거나 만료되었습니다.");
		}
		member.markEmailVerified();
		return MemberMapper.toResponse(memberRepository.save(member));
	}

	private void validate(SignUpCommand command, MemberType type) {
		if (isBlank(command.email()) || isBlank(command.password()) || isBlank(command.name())
			|| isBlank(command.phone())) {
			throw new BusinessException("필수 항목을 모두 입력하세요.");
		}
		rejectDisposableEmailService.requireAllowed(command.email());
		if (!command.password().equals(command.passwordConfirm())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.");
		}
		if (!passwordPolicy.matches(command.password())) {
			throw new BusinessException(passwordPolicy.ruleMessage());
		}
		String phone = digits(command.phone());
		if (!phoneVerificationPolicy.validMobile(phone)) {
			throw new BusinessException("휴대폰 번호는 하이픈 없이 숫자 10~11자리로 입력하세요.");
		}
		if (!Boolean.TRUE.equals(command.termsService()) || !Boolean.TRUE.equals(command.termsPrivacy())) {
			throw new BusinessException("필수 약관에 동의해 주세요.");
		}
		if (type.corporate()) {
			if (!Boolean.TRUE.equals(command.termsCorporate())) {
				throw new BusinessException("법인 대표자 또는 위임받은 대리인 가입 확인에 동의해 주세요.");
			}
			if (isBlank(command.jobTitle())) {
				throw new BusinessException("담당자 부서/직급을 입력하세요.");
			}
			if (isBlank(command.companyName()) || isBlank(command.ceoName())) {
				throw new BusinessException("법인명과 대표자명을 입력하세요.");
			}
			if (digits(command.businessNumber()).length() != 10) {
				throw new BusinessException("사업자등록번호 10자리를 입력하세요.");
			}
			if (digits(command.openingDate()).length() != 8) {
				throw new BusinessException("개업일자를 입력하세요.");
			}
			verifyBusinessRegistrationService.verify(new BusinessVerifyCommand(
				command.businessNumber(),
				command.openingDate(),
				command.ceoName(),
				command.companyName(),
				command.workplaceAddress()
			));
		} else if (isBlank(command.address())) {
			throw new BusinessException("주소를 입력하세요.");
		}
	}

	private void consumePhoneToken(String token, String expected) {
		if (token == null || token.isBlank()) {
			throw new BusinessException("본인 인증을 완료해 주세요.");
		}
		String verified = phoneVerificationStore.consumeToken(token.trim())
			.orElseThrow(() -> new BusinessException("본인 인증이 만료되었습니다. 다시 인증해 주세요."));
		if (!verified.equals(expected)) {
			throw new BusinessException("인증된 정보와 입력한 정보가 일치하지 않습니다.");
		}
	}

	private Member findByLoginOrEmail(String loginId) {
		if (isBlank(loginId)) {
			throw new BusinessException("회원을 찾을 수 없습니다.");
		}
		return memberRepository.findByLoginId(loginId.trim())
			.or(() -> memberRepository.findByEmail(loginId.trim()))
			.orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다."));
	}

	private String digits(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("\\D", "");
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
