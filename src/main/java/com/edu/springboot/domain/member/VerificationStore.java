package com.edu.springboot.domain.member;

import java.time.Duration;
import java.util.Optional;

public interface VerificationStore {

	void saveCode(String key, String code, Duration ttl);

	Optional<String> findCode(String key);

	void deleteCode(String key);

	long cooldownRemainingSeconds(String key);

	void startCooldown(String key, Duration ttl);

	long dailyCount(String bucketKey);

	long incrementDaily(String bucketKey, Duration ttl);

	void saveToken(String token, String targetKey, Duration ttl);

	Optional<String> consumeToken(String token);
}
