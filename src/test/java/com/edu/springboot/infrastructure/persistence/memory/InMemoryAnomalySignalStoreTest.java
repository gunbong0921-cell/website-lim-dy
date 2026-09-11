package com.edu.springboot.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.edu.springboot.domain.anomaly.AnomalyStep;

/**
 * Hexaq
 * 계층: Test
 * 객체: InMemoryAnomalySignalStoreTest
 * 책임: 가입 시각·첫 행동 SETNX·GET 기록·단계
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../../docs/security/07-anomaly-guard.md)
 */
class InMemoryAnomalySignalStoreTest {

	private final InMemoryAnomalySignalStore store = new InMemoryAnomalySignalStore();

	@Test
	@DisplayName("가입 시각을 기록하고 읽는다")
	void rememberSignup_storesEpoch() {
		store.rememberSignup("user", 1_700_000_000_000L, Duration.ofHours(24));

		assertThat(store.signupAt("user")).hasValue(1_700_000_000_000L);
		assertThat(store.signupAt("missing")).isEmpty();
	}

	@Test
	@DisplayName("첫 핵심 API는 한 번만 차지한다")
	void claimFirstCoreAction_once() {
		assertThat(store.claimFirstCoreAction("user", Duration.ofHours(24))).isTrue();
		assertThat(store.claimFirstCoreAction("user", Duration.ofHours(24))).isFalse();
	}

	@Test
	@DisplayName("GET을 남기면 최근 조회가 있다")
	void pushGet_recordsRecentNavigation() {
		store.pushGet("user", "/api/members/me", Duration.ofMinutes(5), 50);

		assertThat(store.hasGetSince("user", System.currentTimeMillis() - 1_000)).isTrue();
		assertThat(store.hasGetSince("other", 0L)).isFalse();
	}

	@Test
	@DisplayName("CONTEXT 단계를 표시한다")
	void markStep_context() {
		store.markStep("user", AnomalyStep.CONTEXT, Duration.ofHours(24));

		assertThat(store.hasStep("user", AnomalyStep.CONTEXT)).isTrue();
		assertThat(store.hasStep("other", AnomalyStep.CONTEXT)).isFalse();
	}

	@Test
	@DisplayName("GET 없이 쓴 횟수를 세고 지울 수 있다")
	void directHits_incrementAndClear() {
		assertThat(store.incrementDirectCoreHits("user", Duration.ofMinutes(5))).isEqualTo(1);
		assertThat(store.incrementDirectCoreHits("user", Duration.ofMinutes(5))).isEqualTo(2);
		store.clearDirectCoreHits("user");
		assertThat(store.incrementDirectCoreHits("user", Duration.ofMinutes(5))).isEqualTo(1);
	}

	@Test
	@DisplayName("같은 계정·API 윈도 횟수를 올린다")
	void incrementWindow_countsHits() {
		Duration window = Duration.ofSeconds(60);

		assertThat(store.incrementWindow("user", "POST:qna", window)).isEqualTo(1);
		assertThat(store.incrementWindow("user", "POST:qna", window)).isEqualTo(2);
		assertThat(store.incrementWindow("user", "POST:like", window)).isEqualTo(1);
	}
}
