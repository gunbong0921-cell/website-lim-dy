package com.edu.springboot.infrastructure.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.edu.springboot.domain.anomaly.AnomalyStep;

/**
 * Hexaq
 * 계층: Test
 * 객체: RedisAnomalySignalStoreTest
 * 책임: ANOMALY: 키 접두
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../../docs/security/07-anomaly-guard.md) · [docs/technical/04-redis.md](../../../../../../../../../docs/technical/04-redis.md)
 */
@ExtendWith(MockitoExtension.class)
class RedisAnomalySignalStoreTest {

	@Mock
	private StringRedisTemplate redis;
	@Mock
	private ValueOperations<String, String> values;

	private RedisAnomalySignalStore store;

	@BeforeEach
	void setUp() {
		store = new RedisAnomalySignalStore(redis);
	}

	@Test
	@DisplayName("가입 시각은 ANOMALY:SIGNUP 키에 둔다")
	void rememberSignup_usesPrefixedKey() {
		when(redis.opsForValue()).thenReturn(values);
		Duration ttl = Duration.ofHours(24);

		store.rememberSignup("user", 123L, ttl);

		verify(values).set("ANOMALY:SIGNUP:user", "123", ttl);
	}

	@Test
	@DisplayName("첫 핵심 API는 SETNX다")
	void claimFirstCoreAction_setIfAbsent() {
		when(redis.opsForValue()).thenReturn(values);
		when(values.setIfAbsent("ANOMALY:FIRST:user", "1", Duration.ofHours(24))).thenReturn(true);

		assertThat(store.claimFirstCoreAction("user", Duration.ofHours(24))).isTrue();
	}

	@Test
	@DisplayName("CONTEXT 단계는 ANOMALY:STEP 키다")
	void markStep_usesStepKey() {
		when(redis.opsForValue()).thenReturn(values);
		Duration ttl = Duration.ofHours(24);

		store.markStep("user", AnomalyStep.CONTEXT, ttl);

		verify(values).set("ANOMALY:STEP:user:CONTEXT", "1", ttl);
	}

	@Test
	@DisplayName("신규 계정 한도는 ANOMALY:NEW_USER 키다")
	void incrementWindow_usesPrefixedKey() {
		when(redis.opsForValue()).thenReturn(values);
		when(values.increment("ANOMALY:NEW_USER:user:POST:qna")).thenReturn(1L);

		assertThat(store.incrementWindow("user", "POST:qna", Duration.ofSeconds(60))).isEqualTo(1);
		verify(redis).expire("ANOMALY:NEW_USER:user:POST:qna", Duration.ofSeconds(60));
	}
}
