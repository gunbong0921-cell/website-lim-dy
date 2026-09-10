package com.edu.springboot.application.member.dto;

public record VerifyCodeResult(String verificationToken, String target) {
}
