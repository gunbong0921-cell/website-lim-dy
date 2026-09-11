package com.edu.springboot.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: EmailPolicyTest
 * 책임: 길이·형식. 일회용 여부는 모름
 * 문서: [docs/security/05-disposable-email.md](../../../../../../../../docs/security/05-disposable-email.md)
 */
class EmailPolicyTest {

	private final EmailPolicy policy = new EmailPolicy();

	@Test
	@DisplayName("정상적인 이메일은 통과한다")
	void validFormat_acceptsNormalAddress() {
		assertThat(policy.validFormat("user@gmail.com")).isTrue();
		assertThat(policy.validFormat("a.b+tag@naver.co.kr")).isTrue();
	}

	@Test
	@DisplayName("빈 값·길이 초과·연속 점은 거절한다")
	void validFormat_rejectsBlankLengthAndDots() {
		assertThat(policy.validFormat(null)).isFalse();
		assertThat(policy.validFormat("  ")).isFalse();
		assertThat(policy.validFormat("a.." + "b@x.com")).isFalse();
		assertThat(policy.validFormat("a".repeat(91) + "@gmail.com")).isFalse();
		assertThat(policy.validFormat(".user@gmail.com")).isFalse();
		assertThat(policy.validFormat("user@.gmail.com")).isFalse();
		assertThat(policy.validFormat("user.@gmail.com")).isFalse();
	}

	@Test
	@DisplayName("도메인은 @ 뒤를 소문자로 꺼낸다")
	void domainOf_takesLowercaseHost() {
		assertThat(policy.domainOf("User@Gmail.COM")).isEqualTo("gmail.com");
		assertThat(policy.domainOf("no-at")).isEmpty();
		assertThat(policy.domainOf(null)).isEmpty();
	}
}
