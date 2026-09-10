package com.edu.springboot.presentation.http;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.security.ClientAddressPolicy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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
