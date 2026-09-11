package com.edu.springboot.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.edu.springboot.domain.member.PhoneVerificationPolicy;

/**
 * Hexaq
 * 계층: Test
 * 객체: InMemoryPhoneVerificationStoreTest
 * 책임: 코드·쿨다운·토큰 1회 소비
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../../docs/security/06-identity-verification.md)
 */
class InMemoryPhoneVerificationStoreTest {

	private final InMemoryPhoneVerificationStore store = new InMemoryPhoneVerificationStore();

	@Test
	@DisplayName("코드를 저장하고 찾을 수 있다")
	void saveAndFindCode() {
		store.saveCode("01012345678", "123456", PhoneVerificationPolicy.CODE_TTL);

		assertThat(store.findCode("01012345678")).contains("123456");
		store.deleteCode("01012345678");
		assertThat(store.findCode("01012345678")).isEmpty();
	}

	@Test
	@DisplayName("토큰은 한 번만 소비된다")
	void consumeToken_once() {
		store.saveToken("tok", "01012345678", PhoneVerificationPolicy.TOKEN_TTL);

		assertThat(store.consumeToken("tok")).contains("01012345678");
		assertThat(store.consumeToken("tok")).isEmpty();
	}

	@Test
	@DisplayName("쿨다운이 있으면 남은 초를 준다")
	void cooldownRemainingSeconds_whileActive() {
		store.startCooldown("01012345678", Duration.ofSeconds(60));

		assertThat(store.cooldownRemainingSeconds("01012345678")).isBetween(1L, 60L);
	}

	@Test
	@DisplayName("일일 카운터를 올린다")
	void incrementDaily_counts() {
		assertThat(store.incrementDaily("bucket", Duration.ofHours(1))).isEqualTo(1);
		assertThat(store.incrementDaily("bucket", Duration.ofHours(1))).isEqualTo(2);
		assertThat(store.dailyCount("bucket")).isEqualTo(2);
	}
}
