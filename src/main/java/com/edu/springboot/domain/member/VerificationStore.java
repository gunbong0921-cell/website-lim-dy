package com.edu.springboot.domain.member;

import java.time.Duration;
import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: VerificationStore
 * 책임: 인증 코드·토큰 포트. Redis/메모리. 회원 원본 아님
 * 문서: [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md)
 */
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
