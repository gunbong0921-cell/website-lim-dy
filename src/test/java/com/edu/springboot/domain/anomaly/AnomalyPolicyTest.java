package com.edu.springboot.domain.anomaly;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: AnomalyPolicyTest
 * 책임: 관리자 통과와 네 규칙 판정
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md)
 */
class AnomalyPolicyTest {

	private final AnomalyPolicy policy = new AnomalyPolicy();

	@Test
	@DisplayName("관리자는 항상 통과한다")
	void decide_allowsAdmin() {
		assertThat(policy.decide(true, true, false, false, 9, 1, true, 0L, 1L, 3000, true, 99, 5))
			.isEqualTo(AnomalyVerdict.ALLOW);
	}

	@Test
	@DisplayName("이미 SUSPICIOUS면 계속 거절한다")
	void decide_rejectsSuspicious() {
		assertThat(allowing().suspicious(true).decide()).isEqualTo(AnomalyVerdict.SUSPICIOUS);
	}

	@Test
	@DisplayName("CONTEXT 단계 없이 쓰면 시퀀스다")
	void decide_sequenceWithoutContext() {
		assertThat(allowing().context(false).decide()).isEqualTo(AnomalyVerdict.SEQUENCE);
	}

	@Test
	@DisplayName("최근 GET 없이 핵심 쓰기를 하면 Zero-Nav다")
	void decide_zeroNavWithoutRecentGet() {
		assertThat(allowing().recentGet(false).directHits(1).directLimit(1).decide())
			.isEqualTo(AnomalyVerdict.ZERO_NAV);
	}

	@Test
	@DisplayName("가입 후 3초 안에 첫 핵심 API면 TTFA다")
	void decide_ttfaWithinThreeSeconds() {
		assertThat(allowing().firstCore(true).signupAt(1000L).now(3999L).ttfa(3000).decide())
			.isEqualTo(AnomalyVerdict.TTFA);
		assertThat(allowing().firstCore(true).signupAt(1000L).now(4000L).ttfa(3000).decide())
			.isEqualTo(AnomalyVerdict.ALLOW);
	}

	@Test
	@DisplayName("신규 계정이 분당 한도를 넘으면 거절한다")
	void decide_newUserRate() {
		assertThat(allowing().newAccount(true).windowHits(6).newUserLimit(5).decide())
			.isEqualTo(AnomalyVerdict.NEW_USER_RATE);
		assertThat(allowing().newAccount(true).windowHits(5).newUserLimit(5).decide())
			.isEqualTo(AnomalyVerdict.ALLOW);
	}

	@Test
	@DisplayName("가입 24시간 이내만 신규 계정이다")
	void newAccount_usesMaxAge() {
		assertThat(policy.newAccount(0L, 23 * 3_600_000L, 24 * 3_600_000L)).isTrue();
		assertThat(policy.newAccount(0L, 24 * 3_600_000L, 24 * 3_600_000L)).isFalse();
		assertThat(policy.newAccount(null, 1L, 24 * 3_600_000L)).isFalse();
	}

	private Case allowing() {
		return new Case();
	}

	private final class Case {
		private boolean admin;
		private boolean suspicious;
		private boolean context = true;
		private boolean recentGet = true;
		private int directHits;
		private int directLimit = 1;
		private boolean firstCore;
		private Long signupAt = 0L;
		private long now = 10_000L;
		private long ttfa = 3000;
		private boolean newAccount;
		private long windowHits;
		private int newUserLimit = 5;

		Case suspicious(boolean value) {
			suspicious = value;
			return this;
		}

		Case context(boolean value) {
			context = value;
			return this;
		}

		Case recentGet(boolean value) {
			recentGet = value;
			return this;
		}

		Case directHits(int value) {
			directHits = value;
			return this;
		}

		Case directLimit(int value) {
			directLimit = value;
			return this;
		}

		Case firstCore(boolean value) {
			firstCore = value;
			return this;
		}

		Case signupAt(Long value) {
			signupAt = value;
			return this;
		}

		Case now(long value) {
			now = value;
			return this;
		}

		Case ttfa(long value) {
			ttfa = value;
			return this;
		}

		Case newAccount(boolean value) {
			newAccount = value;
			return this;
		}

		Case windowHits(long value) {
			windowHits = value;
			return this;
		}

		Case newUserLimit(int value) {
			newUserLimit = value;
			return this;
		}

		AnomalyVerdict decide() {
			return policy.decide(
				admin, suspicious, context, recentGet, directHits, directLimit,
				firstCore, signupAt, now, ttfa, newAccount, windowHits, newUserLimit
			);
		}
	}
}
