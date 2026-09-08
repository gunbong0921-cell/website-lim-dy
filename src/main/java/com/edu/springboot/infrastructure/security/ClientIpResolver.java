package com.edu.springboot.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpResolver {

	private final boolean trustedProxy;

	public ClientIpResolver(@Value("${app.security.trusted-proxy:false}") boolean trustedProxy) {
		this.trustedProxy = trustedProxy;
	}

	public String resolve(HttpServletRequest request) {
		if (trustedProxy) {
			String forwarded = header(request, "X-Forwarded-For");
			if (forwarded != null) {
				return forwarded.split(",")[0].trim();
			}
			String realIp = header(request, "X-Real-IP");
			if (realIp != null) {
				return realIp;
			}
		}
		String remote = request.getRemoteAddr();
		return remote == null || remote.isBlank() ? "unknown" : remote;
	}

	private String header(HttpServletRequest request, String name) {
		String value = request.getHeader(name);
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
