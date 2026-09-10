package com.edu.springboot.domain.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Hexaq
 * 계층: Domain
 * 객체: RequestSignaturePolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../../../../../docs/security/01-hmac-request-signing.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class RequestSignaturePolicy {

	public boolean timestampInWindow(long timestampMillis, long nowMillis, long maxSkewMillis) {
		if (maxSkewMillis < 0) {
			return false;
		}
		return Math.abs(nowMillis - timestampMillis) <= maxSkewMillis;
	}

	public boolean signaturesMatch(String expectedHex, String actualHex) {
		if (expectedHex == null || actualHex == null) {
			return false;
		}
		byte[] expected = expectedHex.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
		byte[] actual = actualHex.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
		return MessageDigest.isEqual(expected, actual);
	}
}
