package com.edu.springboot.domain.security;

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
