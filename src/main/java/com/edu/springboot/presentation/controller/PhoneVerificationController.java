package com.edu.springboot.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.member.SendPhoneVerificationService;
import com.edu.springboot.application.member.VerifyPhoneCodeService;
import com.edu.springboot.application.member.dto.SendVerificationResult;
import com.edu.springboot.application.member.dto.VerifyCodeResult;
import com.edu.springboot.presentation.dto.ApiResponse;
import com.edu.springboot.presentation.http.RequestClientIp;
import com.edu.springboot.presentation.http.RequestHostname;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

	private final SendPhoneVerificationService sendPhoneVerificationService;
	private final VerifyPhoneCodeService verifyPhoneCodeService;
	private final RequestClientIp requestClientIp;
	private final RequestHostname requestHostname;

	@PostMapping("/send-code")
	public ApiResponse<SendVerificationResult> sendCode(@RequestBody PhoneRequest request,
		HttpServletRequest httpRequest) {
		SendVerificationResult data = sendPhoneVerificationService.send(request.phone(),
			requestClientIp.resolve(httpRequest), request.recaptchaToken(), requestHostname.resolve(httpRequest));
		return ApiResponse.ok(data, "인증번호를 발송했습니다. 3분 안에 입력해 주세요.");
	}

	@PostMapping("/verify")
	public ApiResponse<VerifyCodeResult> verify(@RequestBody PhoneVerifyRequest request) {
		VerifyCodeResult data = verifyPhoneCodeService.verify(request.phone(), request.code());
		return ApiResponse.ok(data, "휴대폰 인증이 완료되었습니다.");
	}

	public record PhoneRequest(String phone, String recaptchaToken) {
	}

	public record PhoneVerifyRequest(String phone, String code) {
	}
}
