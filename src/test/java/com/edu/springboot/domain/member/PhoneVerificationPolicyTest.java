package com.edu.springboot.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: PhoneVerificationPolicyTest
 * 책임: TTL·재전송·일 10회·휴대폰 형식
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
class PhoneVerificationPolicyTest {

	private final PhoneVerificationPolicy policy = new PhoneVerificationPolicy();

	@Test
	@DisplayName("TTL·재전송·일일 한도는 문서와 같다")
	void constants_matchDocument() {
		assertThat(PhoneVerificationPolicy.CODE_TTL).isEqualTo(Duration.ofSeconds(180));
		assertThat(PhoneVerificationPolicy.COOLDOWN).isEqualTo(Duration.ofSeconds(60));
		assertThat(PhoneVerificationPolicy.TOKEN_TTL).isEqualTo(Duration.ofMinutes(30));
		assertThat(PhoneVerificationPolicy.DAILY_LIMIT).isEqualTo(10);
	}

	@Test
	@DisplayName("휴대폰은 숫자만 남기고 01x 10~11자리만 통과한다")
	void normalizeAndValidMobile() {
		assertThat(policy.normalize("010-1234-5678")).isEqualTo("01012345678");
		assertThat(policy.validMobile("01012345678")).isTrue();
		assertThat(policy.validMobile("0101234567")).isTrue();
		assertThat(policy.validMobile("0212345678")).isFalse();
		assertThat(policy.validMobile(null)).isFalse();
	}

	@Test
	@DisplayName("인증번호는 6자리이고 문자에 포함된다")
	void newCode_isSixDigitsInMessage() {
		String code = policy.newCode();
		assertThat(code).matches("\\d{6}");
		assertThat(policy.messageBody(code)).contains(code).contains("3분");
	}
}
