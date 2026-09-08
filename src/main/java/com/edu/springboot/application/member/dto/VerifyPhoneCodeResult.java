package com.edu.springboot.application.member.dto;

public record VerifyPhoneCodeResult(String verificationToken, String phone) {
}
