package com.edu.springboot.presentation.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.auth.FindLoginIdService;
import com.edu.springboot.application.auth.PasswordResetService;
import com.edu.springboot.application.member.LoginService;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.infrastructure.security.MemberSessionBinder;
import com.edu.springboot.infrastructure.security.SessionPrincipal;
import com.edu.springboot.presentation.dto.ApiResponse;
import com.edu.springboot.presentation.http.RequestClientIp;
import com.edu.springboot.presentation.http.RequestHostname;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final LoginService loginService;
	private final PasswordResetService passwordResetService;
	private final FindLoginIdService findLoginIdService;
	private final MemberSessionBinder memberSessionBinder;
	private final RequestClientIp requestClientIp;
	private final RequestHostname requestHostname;

	@PostMapping("/login")
	public ApiResponse<MemberResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
		HttpServletResponse httpResponse) {
		MemberResponse member = loginService.authenticate(request.loginId(), request.password(),
			request.recaptchaToken(), requestClientIp.resolve(httpRequest), requestHostname.resolve(httpRequest));
		memberSessionBinder.bind(httpRequest, httpResponse, new SessionPrincipal(member.loginId(), member.admin()));
		return ApiResponse.ok(member, "로그인되었습니다.");
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletRequest request) {
		memberSessionBinder.unbind(request);
		return ApiResponse.ok(null, "로그아웃되었습니다.");
	}

	@PostMapping("/forgot-id")
	public ApiResponse<Map<String, String>> forgotId(@RequestBody Map<String, String> body,
		HttpServletRequest httpRequest) {
		var result = findLoginIdService.sendLoginId(body.get("email"), body.get("recaptchaToken"),
			requestClientIp.resolve(httpRequest), requestHostname.resolve(httpRequest));
		if (result.mailSent()) {
			return ApiResponse.ok(Map.of(), "가입하신 아이디를 이메일로 보냈습니다.");
		}
		return ApiResponse.ok(Map.of("debugLoginId", result.debugLoginId()),
			"메일을 보내지 못했습니다. 아이디를 확인하세요.");
	}

	@PostMapping("/forgot-password")
	public ApiResponse<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body,
		HttpServletRequest httpRequest) {
		var result = passwordResetService.sendTemporaryPassword(body.get("email"), body.get("recaptchaToken"),
			requestClientIp.resolve(httpRequest), requestHostname.resolve(httpRequest));
		if (result.mailSent()) {
			return ApiResponse.ok(Map.of(), "임시 비밀번호를 이메일로 보냈습니다.");
		}
		return ApiResponse.ok(Map.of("debugPassword", result.debugPassword()),
			"메일을 보내지 못했습니다. 임시 비밀번호를 확인하세요.");
	}

	public record LoginRequest(String loginId, String password, String recaptchaToken) {
	}
}
