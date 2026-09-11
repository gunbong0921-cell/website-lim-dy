package com.edu.springboot.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: InMemoryRequestTicketStoreTest
 * 책임: 일회 소비·만료·공백 티켓
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../../docs/security/01-hmac-request-signing.md)
 */
class InMemoryRequestTicketStoreTest {

	private final InMemoryRequestTicketStore store = new InMemoryRequestTicketStore();

	@Test
	@DisplayName("저장한 키를 한 번만 꺼낸다")
	void consume_returnsKeyOnce() {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));

		assertThat(store.consume("ticket-1")).contains("signing-key");
		assertThat(store.consume("ticket-1")).isEmpty();
	}

	@Test
	@DisplayName("소비 시 티켓 공백을 제거한다")
	void consume_trimsTicketId() {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(60));

		assertThat(store.consume(" ticket-1 ")).contains("signing-key");
	}

	@Test
	@DisplayName("없거나 공백인 티켓은 비어 있다")
	void consume_rejectsUnknownOrBlank() {
		assertThat(store.consume("missing")).isEmpty();
		assertThat(store.consume(null)).isEmpty();
		assertThat(store.consume("  ")).isEmpty();
	}

	@Test
	@DisplayName("이미 만료된 티켓은 소비하지 않는다")
	void consume_rejectsExpired() {
		store.save("ticket-1", "signing-key", Duration.ofSeconds(-1));

		assertThat(store.consume("ticket-1")).isEmpty();
	}
}
