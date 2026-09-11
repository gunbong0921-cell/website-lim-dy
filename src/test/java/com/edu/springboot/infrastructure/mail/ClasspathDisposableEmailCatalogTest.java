package com.edu.springboot.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: ClasspathDisposableEmailCatalogTest
 * 책임: 클래스패스 목록·하위 도메인. GitHub 실호출 없음
 * 문서: [docs/security/05-disposable-email.md](../../../../../../../../docs/security/05-disposable-email.md)
 */
class ClasspathDisposableEmailCatalogTest {

	private final ClasspathDisposableEmailCatalog catalog = new ClasspathDisposableEmailCatalog();

	@Test
	@DisplayName("목록에 있는 도메인과 그 하위 도메인을 막는다")
	void blocked_matchesListedDomainAndSubdomain() {
		assertThat(catalog.blocked("mailinator.com")).isTrue();
		assertThat(catalog.blocked("foo.mailinator.com")).isTrue();
		assertThat(catalog.blocked("10minutemail.com")).isTrue();
		assertThat(catalog.blocked("guerrillamail.com")).isTrue();
	}

	@Test
	@DisplayName("일반 메일과 TLD는 막지 않는다")
	void blocked_allowsRealProvidersAndBareTld() {
		assertThat(catalog.blocked("gmail.com")).isFalse();
		assertThat(catalog.blocked("naver.com")).isFalse();
		assertThat(catalog.blocked("com")).isFalse();
		assertThat(catalog.blocked("  ")).isFalse();
		assertThat(catalog.blocked(null)).isFalse();
	}
}
