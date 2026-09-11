package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.VerifyCodeResult;
import com.edu.springboot.domain.member.PhoneVerificationPolicy;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryPhoneVerificationStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: VerifyCodeServiceTest
 * 책임: 이메일·휴대폰 6자리 확인 후 일회용 토큰
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
class VerifyCodeServiceTest {

	private final InMemoryPhoneVerificationStore store = new InMemoryPhoneVerificationStore();
	private final PhoneVerificationPolicy policy = new PhoneVerificationPolicy();
	private VerifyPhoneCodeService verifyPhone;
	private VerifyEmailCodeService verifyEmail;

	@BeforeEach
	void setUp() {
		verifyPhone = new VerifyPhoneCodeService(store, policy);
		verifyEmail = new VerifyEmailCodeService(store, policy);
	}

	@Test
	@DisplayName("휴대폰 6자리가 맞으면 토큰을 주고 코드를 지운다")
	void verifyPhone_issuesOneTimeToken() {
		store.saveCode("01012345678", "123456", PhoneVerificationPolicy.CODE_TTL);

		VerifyCodeResult result = verifyPhone.verify("010-1234-5678", "123456");

		assertThat(result.verificationToken()).isNotBlank();
		assertThat(result.target()).isEqualTo("01012345678");
		assertThat(store.findCode("01012345678")).isEmpty();
		assertThat(store.consumeToken(result.verificationToken())).contains("01012345678");
	}

	@Test
	@DisplayName("이메일 6자리가 맞으면 MAIL 키로 토큰을 준다")
	void verifyEmail_issuesTokenForMailKey() {
		store.saveCode("MAIL:user@gmail.com", "654321", PhoneVerificationPolicy.CODE_TTL);

		VerifyCodeResult result = verifyEmail.verify("  User@Gmail.COM ", "654321");

		assertThat(result.target()).isEqualTo("user@gmail.com");
		assertThat(store.findCode("MAIL:user@gmail.com")).isEmpty();
		assertThat(store.consumeToken(result.verificationToken())).contains("MAIL:user@gmail.com");
	}

	@Test
	@DisplayName("코드가 틀리거나 6자리가 아니면 거절한다")
	void verify_rejectsWrongOrMalformedCode() {
		store.saveCode("01012345678", "123456", PhoneVerificationPolicy.CODE_TTL);

		assertThatThrownBy(() -> verifyPhone.verify("01012345678", "000000"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("인증번호가 올바르지 않습니다.");
		assertThatThrownBy(() -> verifyPhone.verify("01012345678", "12"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("인증번호 6자리를 입력하세요.");
		assertThatThrownBy(() -> verifyEmail.verify("user@gmail.com", "111111"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("인증번호가 만료되었거나 발송되지 않았습니다.");
	}
}
