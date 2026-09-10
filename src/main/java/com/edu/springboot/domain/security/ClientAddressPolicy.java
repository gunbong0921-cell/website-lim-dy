package com.edu.springboot.domain.security;

/**
 * Hexaq
 * 계층: Domain
 * 객체: ClientAddressPolicy
 * 책임: 신뢰 프록시일 때만 포워드 헤더로 IP·호스트 결정. HTTP 모름
 * 문서: [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class ClientAddressPolicy {

	private final boolean trustedProxy;

	public ClientAddressPolicy(boolean trustedProxy) {
		this.trustedProxy = trustedProxy;
	}

	public String resolve(String remoteAddr, String forwardedFor, String realIp) {
		if (trustedProxy) {
			String forwarded = first(forwardedFor);
			if (forwarded != null) {
				return forwarded.split(",")[0].trim();
			}
			String real = first(realIp);
			if (real != null) {
				return real;
			}
		}
		return blank(remoteAddr) ? "unknown" : remoteAddr.trim();
	}

	public String resolveHost(String serverName, String forwardedHost) {
		if (trustedProxy) {
			String forwarded = first(forwardedHost);
			if (forwarded != null) {
				return forwarded.split(",")[0].trim();
			}
		}
		return blank(serverName) ? "" : serverName.trim();
	}

	private static String first(String value) {
		return blank(value) ? null : value.trim();
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
