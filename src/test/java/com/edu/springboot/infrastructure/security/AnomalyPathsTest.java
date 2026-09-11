package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: AnomalyPathsTest
 * 책임: 핵심 쓰기와 CONTEXT GET 경로
 * 문서: [docs/security/07-anomaly-guard.md](../../../../../../../../docs/security/07-anomaly-guard.md)
 */
class AnomalyPathsTest {

	@Test
	@DisplayName("로그인 회원 게시글·댓글·좋아요 쓰기는 핵심 API다")
	void coreWrite_memberMutations() {
		assertThat(AnomalyPaths.coreWrite("POST", "/api/boards/qna")).isTrue();
		assertThat(AnomalyPaths.coreWrite("PUT", "/api/boards/archive/1")).isTrue();
		assertThat(AnomalyPaths.coreWrite("DELETE", "/api/comments/1")).isTrue();
		assertThat(AnomalyPaths.coreWrite("POST", "/api/boards/free/1/like")).isTrue();
		assertThat(AnomalyPaths.coreWrite("POST", "/api/boards/free")).isTrue();
	}

	@Test
	@DisplayName("조회와 가입 API는 핵심 쓰기가 아니다")
	void coreWrite_skipsGetsAndSignup() {
		assertThat(AnomalyPaths.coreWrite("GET", "/api/boards/qna")).isFalse();
		assertThat(AnomalyPaths.coreWrite("POST", "/api/members/signup")).isFalse();
		assertThat(AnomalyPaths.coreWrite("POST", "/api/auth/login")).isFalse();
	}

	@Test
	@DisplayName("GET /me와 게시판 GET은 CONTEXT 조회다")
	void contextGet_meAndBoards() {
		assertThat(AnomalyPaths.contextGet("GET", "/api/members/me")).isTrue();
		assertThat(AnomalyPaths.contextGet("GET", "/api/boards/free")).isTrue();
		assertThat(AnomalyPaths.contextGet("POST", "/api/members/me")).isFalse();
	}

	@Test
	@DisplayName("apiName은 동사와 자원을 붙인다")
	void apiName_usesVerbAndResource() {
		assertThat(AnomalyPaths.apiName("POST", "/api/boards/qna")).isEqualTo("POST:qna");
		assertThat(AnomalyPaths.apiName("POST", "/api/boards/free/1/like")).isEqualTo("POST:like");
	}
}
