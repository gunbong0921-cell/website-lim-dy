package com.edu.springboot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hexaq
 * 계층: Test
 * 객체: HmacSha256Test
 * 책임: HMAC-SHA256·SHA-256 hex가 공개 벡터와 같다
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md)
 */
class HmacSha256Test {

	@Test
	@DisplayName("HMAC-SHA256 hex는 소문자이고 공개 벡터와 같다")
	void hmacHex_matchesKnownVector() {
		assertThat(HmacSha256.hmacHex("key", "The quick brown fox jumps over the lazy dog"))
			.isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
	}

	@Test
	@DisplayName("빈 바디 SHA-256은 공개 벡터와 같다")
	void sha256Hex_emptyAndNullMatchEmptyDigest() {
		String empty = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
		assertThat(HmacSha256.sha256Hex(new byte[0])).isEqualTo(empty);
		assertThat(HmacSha256.sha256Hex(null)).isEqualTo(empty);
	}

	@Test
	@DisplayName("abc의 SHA-256은 공개 벡터와 같다")
	void sha256Hex_matchesAbcVector() {
		assertThat(HmacSha256.sha256Hex("abc".getBytes(StandardCharsets.UTF_8)))
			.isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
	}
}
