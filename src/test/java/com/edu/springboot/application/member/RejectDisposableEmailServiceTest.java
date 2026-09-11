package com.edu.springboot.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.member.DisposableEmailCatalog;
import com.edu.springboot.domain.member.EmailPolicy;

/**
 * Hexaq
 * 계층: Test
 * 객체: RejectDisposableEmailServiceTest
 * 책임: 소문자 정규화 → 형식 → 카탈로그. DB 모름
 * 문서: [docs/security/05-disposable-email.md](../../../../../../../../docs/security/05-disposable-email.md)
 */
class RejectDisposableEmailServiceTest {

	private final DisposableEmailCatalog catalog = domain -> "mailinator.com".equals(domain);
	private final RejectDisposableEmailService service =
		new RejectDisposableEmailService(new EmailPolicy(), catalog);

	@Test
	@DisplayName("허용 메일은 소문자로 정규화해 돌려준다")
	void requireAllowed_normalizesCase() {
		assertThat(service.requireAllowed("  User@Gmail.COM ")).isEqualTo("user@gmail.com");
	}

	@Test
	@DisplayName("형식이 틀리면 형식 메시지로 거절한다")
	void requireAllowed_rejectsInvalidFormat() {
		assertThatThrownBy(() -> service.requireAllowed("not-an-email"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("이메일 형식이 올바르지 않습니다.");
	}

	@Test
	@DisplayName("일회용 도메인이면 카탈로그 메시지로 거절한다")
	void requireAllowed_rejectsBlockedDomain() {
		assertThatThrownBy(() -> service.requireAllowed("bot@mailinator.com"))
			.isInstanceOf(BusinessException.class)
			.hasMessage("일회용 이메일은 사용할 수 없습니다.");
	}
}
