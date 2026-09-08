package com.edu.springboot.application.member.dto;

public record SignUpResult(Long memberId, boolean mailSent, String debugCode) {
}
