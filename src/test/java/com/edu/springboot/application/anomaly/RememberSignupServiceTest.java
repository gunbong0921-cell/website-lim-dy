package com.edu.springboot.application.anomaly;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: RememberSignupServiceTest
 * 책임: 가입 직후 시각 기록
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md)
 */
@ExtendWith(MockitoExtension.class)
class RememberSignupServiceTest {

	@Mock
	private AnomalySignalStore anomalySignalStore;

	@Test
	@DisplayName("loginId를 정규화해 24시간 TTL로 저장한다")
	void remember_storesTrimmedLoginId() {
		RememberSignupService service = new RememberSignupService(anomalySignalStore, 24);

		service.remember("  user@gmail.com ");

		verify(anomalySignalStore).rememberSignup(eq("user@gmail.com"), anyLong(), eq(Duration.ofHours(24)));
	}

	@Test
	@DisplayName("빈 loginId는 저장하지 않는다")
	void remember_skipsBlank() {
		RememberSignupService service = new RememberSignupService(anomalySignalStore, 24);

		service.remember("  ");

		verify(anomalySignalStore, never()).rememberSignup(anyString(), anyLong(), any());
	}
}
