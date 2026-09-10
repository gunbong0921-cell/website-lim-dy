package com.edu.springboot.domain.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

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
