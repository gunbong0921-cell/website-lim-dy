package com.edu.springboot.infrastructure.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.edu.springboot.domain.member.PhoneVerificationPolicy;

/**
 * Hexaq
 * 계층: Test
 * 객체: RedisPhoneVerificationStoreTest
 * 책임: PHONE_VERIFY 접두·토큰 GETDEL
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/04-redis.md](../../../../../../../../../docs/technical/04-redis.md)
 */
@ExtendWith(MockitoExtension.class)
class RedisPhoneVerificationStoreTest {

	@Mock
	private StringRedisTemplate redis;
	@Mock
	private ValueOperations<String, String> values;

	private RedisPhoneVerificationStore store;

	@BeforeEach
	void setUp() {
		store = new RedisPhoneVerificationStore(redis);
	}

	@Test
	@DisplayName("코드 저장은 PHONE_VERIFY 접두와 TTL을 쓴다")
	void saveCode_usesPrefixedKey() {
		when(redis.opsForValue()).thenReturn(values);

		store.saveCode("01012345678", "123456", PhoneVerificationPolicy.CODE_TTL);

		verify(values).set("PHONE_VERIFY:01012345678", "123456", PhoneVerificationPolicy.CODE_TTL);
	}

	@Test
	@DisplayName("토큰 소비는 GETDEL에 해당한다")
	void consumeToken_usesGetAndDelete() {
		when(redis.opsForValue()).thenReturn(values);
		when(values.getAndDelete("PHONE_TOKEN:tok")).thenReturn("01012345678");

		assertThat(store.consumeToken("tok")).contains("01012345678");
	}

	@Test
	@DisplayName("쿨다운 TTL이 없으면 0초다")
	void cooldownRemainingSeconds_zeroWhenMissing() {
		when(redis.getExpire("PHONE_VERIFY_COOLDOWN:01012345678", TimeUnit.SECONDS)).thenReturn(-1L);

		assertThat(store.cooldownRemainingSeconds("01012345678")).isZero();
	}
}
