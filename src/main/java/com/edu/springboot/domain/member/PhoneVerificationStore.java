package com.edu.springboot.domain.member;

import java.time.Duration;
import java.util.Optional;

public interface PhoneVerificationStore {

	void saveCode(String phone, String code, Duration ttl);

	Optional<String> findCode(String phone);

	void deleteCode(String phone);

	long cooldownRemainingSeconds(String phone);

	void startCooldown(String phone, Duration ttl);

	long dailyCount(String bucketKey);

	long incrementDaily(String bucketKey, Duration ttl);

	void saveToken(String token, String phone, Duration ttl);

	Optional<String> consumeToken(String token);
}
