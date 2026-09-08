package com.edu.springboot.presentation.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.member.MemberProfileService;
import com.edu.springboot.application.member.SignUpService;
import com.edu.springboot.application.member.VerifyBusinessRegistrationService;
import com.edu.springboot.application.member.dto.BusinessVerifyCommand;
import com.edu.springboot.application.member.dto.BusinessVerifyResponse;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.application.member.dto.SignUpCommand;
import com.edu.springboot.application.member.dto.SignUpResult;
import com.edu.springboot.presentation.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final SignUpService signUpService;
	private final VerifyBusinessRegistrationService verifyBusinessRegistrationService;
	private final MemberProfileService memberProfileService;

	@GetMapping("/check-id")
	public ApiResponse<Map<String, Boolean>> checkId(@RequestParam("loginId") String loginId) {
		boolean available = signUpService.isLoginIdAvailable(loginId);
		return ApiResponse.ok(Map.of("available", available),
			available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.");
	}

	@GetMapping("/check-email")
	public ApiResponse<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
		boolean available = signUpService.isEmailAvailable(email);
		return ApiResponse.ok(Map.of("available", available),
			available ? "사용 가능한 이메일입니다." : "이미 등록된 이메일입니다.");
	}

	@PostMapping("/business/verify")
	public ApiResponse<BusinessVerifyResponse> verifyBusiness(@RequestBody BusinessVerifyCommand command) {
		BusinessVerifyResponse data = verifyBusinessRegistrationService.verify(command);
		String status = data.statusName() == null || data.statusName().isBlank() ? "확인됨" : data.statusName();
		String tax = data.taxType() == null || data.taxType().isBlank() ? "" : " · " + data.taxType();
		return ApiResponse.ok(data, "국세청 확인: " + status + tax);
	}

	@PostMapping("/signup")
	public ApiResponse<SignUpResult> signUp(@RequestBody SignUpCommand command) {
		SignUpResult result = signUpService.signUp(command);
		String kind = "PHONE".equals(result.verificationChannel()) ? "휴대폰" : "이메일";
		return ApiResponse.ok(result, kind + " 인증이 완료된 계정으로 가입되었습니다. 로그인하세요.");
	}

	@PostMapping("/resend-verification")
	public ApiResponse<SignUpResult> resend(@RequestBody Map<String, String> body) {
		SignUpResult result = signUpService.resendVerification(body.get("loginId"));
		String message = result.mailSent()
			? "인증 코드를 다시 보냈습니다."
			: "메일을 보내지 못했습니다. 화면에 표시된 인증 코드로 인증하세요.";
		return ApiResponse.ok(result, message);
	}

	@PostMapping("/verify-email")
	public ApiResponse<MemberResponse> verify(@RequestBody Map<String, String> body) {
		return ApiResponse.ok(signUpService.verifyEmail(body.get("loginId"), body.get("code")), "이메일 인증이 완료되었습니다.");
	}

	@GetMapping("/me")
	public ApiResponse<MemberResponse> me(Authentication authentication) {
		return ApiResponse.ok(memberProfileService.me(authentication.getName()));
	}

	@PutMapping("/profile")
	public ApiResponse<MemberResponse> update(Authentication authentication, @RequestBody ProfileRequest request) {
		return ApiResponse.ok(
			memberProfileService.update(
				authentication.getName(),
				request.name(),
				request.phone(),
				request.company(),
				request.address(),
				request.jobTitle(),
				request.workplaceAddress()
			),
			"회원정보가 수정되었습니다."
		);
	}

	@PutMapping("/password")
	public ApiResponse<Void> changePassword(Authentication authentication, @RequestBody PasswordRequest request) {
		memberProfileService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
		return ApiResponse.ok(null, "비밀번호가 변경되었습니다.");
	}

	public record ProfileRequest(String name, String phone, String company, String address, String jobTitle,
		String workplaceAddress) {
	}

	public record PasswordRequest(String currentPassword, String newPassword) {
	}
}
