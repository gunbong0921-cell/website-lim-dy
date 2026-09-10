package com.edu.springboot.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: HmacSha256
 * 책임: HmacSha256 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public final class HmacSha256 {

	private HmacSha256() {
	}

	public static String hmacHex(String key, String message) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("HMAC-SHA256 unavailable", ex);
		}
	}

	public static String sha256Hex(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(data == null ? new byte[0] : data));
		} catch (Exception ex) {
			throw new IllegalStateException("SHA-256 unavailable", ex);
		}
	}
}
