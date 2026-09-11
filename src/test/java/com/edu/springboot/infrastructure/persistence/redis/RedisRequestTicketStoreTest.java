package com.edu.springboot.infrastructure.persistence.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

/**
 * Hexaq
 * 계층: Test
 * 객체: RedisRequestTicketStoreTest
 * 책임: REQ_TICKET 키·GETDEL 위임
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../../docs/security/01-hmac-request-signing.md) · [docs/technical/04-redis.md](../../../../../../../../../docs/technical/04-redis.md)
 */
@ExtendWith(MockitoExtension.class)
class RedisRequestTicketStoreTest {

	@Mock
	private StringRedisTemplate redis;

	@Mock
	private ValueOperations<String, String> values;

	private RedisRequestTicketStore store;

	@BeforeEach
	void setUp() {
		store = new RedisRequestTicketStore(redis);
	}

	@Test
	@DisplayName("저장은 REQ_TICKET 접두와 TTL을 쓴다")
	void save_usesPrefixedKeyAndTtl() {
		when(redis.opsForValue()).thenReturn(values);
		Duration ttl = Duration.ofSeconds(60);

		store.save("ticket-1", "signing-key", ttl);

		verify(values).set("REQ_TICKET:ticket-1", "signing-key", ttl);
	}

	@Test
	@DisplayName("소비는 GETDEL에 해당한다")
	void consume_usesGetAndDelete() {
		when(redis.opsForValue()).thenReturn(values);
		when(values.getAndDelete("REQ_TICKET:ticket-1")).thenReturn("signing-key");

		assertThat(store.consume(" ticket-1 ")).contains("signing-key");
	}

	@Test
	@DisplayName("공백 티켓은 Redis를 부르지 않는다")
	void consume_skipsRedisForBlankTicket() {
		assertThat(store.consume(null)).isEmpty();
		assertThat(store.consume("  ")).isEmpty();
		verify(values, never()).getAndDelete(any());
	}
}
