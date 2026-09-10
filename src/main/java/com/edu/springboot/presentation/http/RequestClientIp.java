package com.edu.springboot.presentation.http;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.security.ClientAddressPolicy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: RequestClientIp
 * 책임: RemoteAddr / X-Forwarded-For 만 꺼내 ClientAddressPolicy 에 전달
 * 문서: [docs/security/02-rate-limiting.md](../../../../../../../../docs/security/02-rate-limiting.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
@RequiredArgsConstructor
public class RequestClientIp {

	private final ClientAddressPolicy clientAddressPolicy;

	public String resolve(HttpServletRequest request) {
		return clientAddressPolicy.resolve(
			request.getRemoteAddr(),
			request.getHeader("X-Forwarded-For"),
			request.getHeader("X-Real-IP")
		);
	}
}
