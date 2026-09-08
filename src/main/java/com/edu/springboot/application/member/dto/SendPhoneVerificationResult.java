package com.edu.springboot.application.member.dto;

public record SendPhoneVerificationResult(int cooldownSeconds, int expiresInSeconds) {
}
