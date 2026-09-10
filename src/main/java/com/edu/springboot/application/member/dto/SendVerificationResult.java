package com.edu.springboot.application.member.dto;

public record SendVerificationResult(int cooldownSeconds, int expiresInSeconds) {
}
