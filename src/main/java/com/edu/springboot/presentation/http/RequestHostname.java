package com.edu.springboot.presentation.http;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.security.ClientAddressPolicy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: RequestHostname
 * 책임: Host / X-Forwarded-Host 만 꺼내 ClientAddressPolicy 에 전달
 * 문서: [docs/technical/03-cloudflare-tunnel.md](../../../../../../../../docs/technical/03-cloudflare-tunnel.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@RequiredArgsConstructor
public class RequestHostname {

	private final ClientAddressPolicy clientAddressPolicy;

	public String resolve(HttpServletRequest request) {
		return clientAddressPolicy.resolveHost(
			request.getServerName(),
			request.getHeader("X-Forwarded-Host")
		);
	}
}
