package com.edu.springboot.application.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edu.springboot.domain.security.RequestTicketStore;

/**
 * Hexaq
 * 계층: Test
 * 객체: IssueRequestTicketServiceTest
 * 책임: 일회 티켓 발급과 스토어 저장
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md)
 */
@ExtendWith(MockitoExtension.class)
class IssueRequestTicketServiceTest {

	@Mock
	private RequestTicketStore requestTicketStore;

	@Test
	@DisplayName("티켓 id·키·만료를 만들고 스토어에 저장한다")
	void issue_persistsTicketAndReturnsPublicDto() {
		IssueRequestTicketService service = new IssueRequestTicketService(requestTicketStore, 60);
		Instant before = Instant.now();

		IssuedRequestTicket issued = service.issue();

		assertThat(issued.ticketId()).isNotBlank();
		assertThat(issued.signingKey()).matches("[0-9a-f]{64}");
		assertThat(issued.expiresAt()).isAfter(before);
		assertThat(issued.expiresAt()).isBeforeOrEqualTo(before.plusSeconds(61));

		ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
		verify(requestTicketStore).save(id.capture(), key.capture(), eq(Duration.ofSeconds(60)));
		assertThat(id.getValue()).isEqualTo(issued.ticketId());
		assertThat(key.getValue()).isEqualTo(issued.signingKey());
	}

	@Test
	@DisplayName("TTL이 1초 미만이면 1초로 올린다")
	void issue_clampsTtlToAtLeastOneSecond() {
		IssueRequestTicketService service = new IssueRequestTicketService(requestTicketStore, 0);

		service.issue();

		verify(requestTicketStore).save(anyString(), anyString(), eq(Duration.ofSeconds(1)));
	}
}
