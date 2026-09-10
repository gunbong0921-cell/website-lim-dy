package com.edu.springboot.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.security.RequestTicketStore;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryRequestTicketStore;
import com.edu.springboot.infrastructure.persistence.redis.RedisRequestTicketStore;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: RequestTicketStoreConfig
 * 책임: 스프링 설정. 구현 빈을 포트에 꽂음
 * 문서: [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md)
 */
@Configuration
public class RequestTicketStoreConfig {

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "redis")
	RequestTicketStore redisRequestTicketStore(StringRedisTemplate redis) {
		return new RedisRequestTicketStore(redis);
	}

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "memory", matchIfMissing = true)
	RequestTicketStore inMemoryRequestTicketStore() {
		return new InMemoryRequestTicketStore();
	}
}
