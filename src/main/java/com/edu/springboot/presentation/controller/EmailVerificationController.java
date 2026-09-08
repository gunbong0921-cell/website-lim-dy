package com.edu.springboot.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.member.SendEmailVerificationService;
import com.edu.springboot.application.member.VerifyEmailCodeService;
import com.edu.springboot.application.member.dto.SendPhoneVerificationResult;
import com.edu.springboot.application.member.dto.VerifyPhoneCodeResult;
import com.edu.springboot.presentation.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/email")
@RequiredArgsConstructor
public class EmailVerificationController {

	private final SendEmailVerificationService sendEmailVerificationService;
	private final VerifyEmailCodeService verifyEmailCodeService;

	@PostMapping("/send-code")
	public ApiResponse<SendPhoneVerificationResult> sendCode(@RequestBody EmailRequest request,
		HttpServletRequest httpRequest) {
		SendPhoneVerificationResult data = sendEmailVerificationService.send(request.email(), clientIp(httpRequest));
		return ApiResponse.ok(data, "인증번호를 이메일로 보냈습니다. 3분 안에 입력해 주세요.");
	}

	@PostMapping("/verify")
	public ApiResponse<VerifyPhoneCodeResult> verify(@RequestBody EmailVerifyRequest request) {
		VerifyPhoneCodeResult data = verifyEmailCodeService.verify(request.email(), request.code());
		return ApiResponse.ok(data, "이메일 인증이 완료되었습니다.");
	}

	private String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
	}

	public record EmailRequest(String email) {
	}

	public record EmailVerifyRequest(String email, String code) {
	}
}
