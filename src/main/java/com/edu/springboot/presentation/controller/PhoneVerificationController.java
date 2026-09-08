package com.edu.springboot.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.member.SendPhoneVerificationService;
import com.edu.springboot.application.member.VerifyPhoneCodeService;
import com.edu.springboot.application.member.dto.SendPhoneVerificationResult;
import com.edu.springboot.application.member.dto.VerifyPhoneCodeResult;
import com.edu.springboot.presentation.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

	private final SendPhoneVerificationService sendPhoneVerificationService;
	private final VerifyPhoneCodeService verifyPhoneCodeService;

	@PostMapping("/send-code")
	public ApiResponse<SendPhoneVerificationResult> sendCode(@RequestBody PhoneRequest request,
		HttpServletRequest httpRequest) {
		SendPhoneVerificationResult data = sendPhoneVerificationService.send(request.phone(), clientIp(httpRequest));
		return ApiResponse.ok(data, "인증번호를 발송했습니다. 3분 안에 입력해 주세요.");
	}

	@PostMapping("/verify")
	public ApiResponse<VerifyPhoneCodeResult> verify(@RequestBody PhoneVerifyRequest request) {
		VerifyPhoneCodeResult data = verifyPhoneCodeService.verify(request.phone(), request.code());
		return ApiResponse.ok(data, "휴대폰 인증이 완료되었습니다.");
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

	public record PhoneRequest(String phone) {
	}

	public record PhoneVerifyRequest(String phone, String code) {
	}
}
