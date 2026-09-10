package com.edu.springboot.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.member.VerificationStore;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryPhoneVerificationStore;
import com.edu.springboot.infrastructure.persistence.redis.RedisPhoneVerificationStore;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: PhoneVerificationStoreConfig
 * 책임: 스프링 설정. 구현 빈을 포트에 꽂음
 * 문서: [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md)
 */
@Configuration
public class PhoneVerificationStoreConfig {

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "redis")
	VerificationStore redisPhoneVerificationStore(StringRedisTemplate redis) {
		return new RedisPhoneVerificationStore(redis);
	}

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "memory", matchIfMissing = true)
	VerificationStore inMemoryPhoneVerificationStore() {
		return new InMemoryPhoneVerificationStore();
	}
}
