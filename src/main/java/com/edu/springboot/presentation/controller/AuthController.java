package com.edu.springboot.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.auth.PasswordResetService;
import com.edu.springboot.application.member.LoginService;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.presentation.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final LoginService loginService;
	private final PasswordResetService passwordResetService;
	private final SecurityContextRepository securityContextRepository;

	@PostMapping("/login")
	public ApiResponse<MemberResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
		HttpServletResponse httpResponse) {
		MemberResponse member = loginService.authenticate(request.loginId(), request.password());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			member.loginId(),
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);
		return ApiResponse.ok(member, "로그인되었습니다.");
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletRequest request) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ApiResponse.ok(null, "로그아웃되었습니다.");
	}

	@PostMapping("/forgot-password")
	public ApiResponse<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
		var result = passwordResetService.sendTemporaryPassword(body.get("email"));
		if (result.mailSent()) {
			return ApiResponse.ok(Map.of(), "임시 비밀번호를 이메일로 보냈습니다.");
		}
		return ApiResponse.ok(Map.of("debugPassword", result.debugPassword()),
			"메일을 보내지 못했습니다. 임시 비밀번호를 확인하세요.");
	}

	public record LoginRequest(String loginId, String password) {
	}
}
