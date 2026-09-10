package com.edu.springboot.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.security.RequestTicketStore;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryRequestTicketStore;
import com.edu.springboot.infrastructure.persistence.redis.RedisRequestTicketStore;

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
