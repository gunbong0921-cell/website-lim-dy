package com.edu.springboot.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.captcha.GetRecaptchaPublicConfigService;
import com.edu.springboot.application.captcha.RecaptchaPublicConfig;
import com.edu.springboot.application.security.IssueRequestTicketService;
import com.edu.springboot.application.security.IssuedRequestTicket;
import com.edu.springboot.presentation.dto.ApiResponse;
import com.edu.springboot.presentation.http.RequestHostname;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: PublicConfigController
 * 책임: HTTP 입구. 검증·Application 호출·ApiResponse. Domain 객체 비노출
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicConfigController {

	private final GetRecaptchaPublicConfigService getRecaptchaPublicConfigService;
	private final IssueRequestTicketService issueRequestTicketService;
	private final RequestHostname requestHostname;

	@GetMapping("/config")
	public ApiResponse<RecaptchaPublicConfig> config(HttpServletRequest request) {
		return ApiResponse.ok(getRecaptchaPublicConfigService.get(requestHostname.resolve(request)));
	}

	@GetMapping("/request-ticket")
	public ApiResponse<IssuedRequestTicket> requestTicket() {
		return ApiResponse.ok(issueRequestTicketService.issue());
	}
}
