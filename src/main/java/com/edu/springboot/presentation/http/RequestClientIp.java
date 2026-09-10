package com.edu.springboot.presentation.http;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.security.ClientAddressPolicy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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
