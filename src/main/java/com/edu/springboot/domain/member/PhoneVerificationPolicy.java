package com.edu.springboot.domain.member;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class PhoneVerificationPolicy {

	public static final Duration CODE_TTL = Duration.ofSeconds(180);
	public static final Duration COOLDOWN = Duration.ofSeconds(60);
	public static final Duration TOKEN_TTL = Duration.ofMinutes(30);
	public static final int DAILY_LIMIT = 10;

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Pattern MOBILE = Pattern.compile("01[016789]\\d{7,8}");
	private static final SecureRandom RANDOM = new SecureRandom();

	public String normalize(String raw) {
		if (raw == null) {
			return "";
		}
		return raw.replaceAll("\\D", "");
	}

	public boolean validMobile(String phone) {
		return phone != null && MOBILE.matcher(phone).matches();
	}

	public String newCode() {
		return String.format("%06d", RANDOM.nextInt(1_000_000));
	}

	public String messageBody(String code) {
		return "[Hexaq] 인증번호는 [" + code + "]입니다. 3분 내에 입력해주세요.";
	}

	public String dailyPhoneKey(String phone) {
		return "PHONE_VERIFY_DAILY:" + today() + ":" + phone;
	}

	public String dailyIpKey(String ip) {
		String value = ip == null || ip.isBlank() ? "unknown" : ip.trim();
		return "PHONE_VERIFY_DAILY_IP:" + today() + ":" + value;
	}

	public Duration ttlUntilMidnight() {
		ZonedDateTime now = ZonedDateTime.now(SEOUL);
		ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(SEOUL);
		Duration ttl = Duration.between(now, midnight);
		return ttl.getSeconds() < 1 ? Duration.ofDays(1) : ttl;
	}

	private String today() {
		return LocalDate.now(SEOUL).toString().replace("-", "");
	}
}
