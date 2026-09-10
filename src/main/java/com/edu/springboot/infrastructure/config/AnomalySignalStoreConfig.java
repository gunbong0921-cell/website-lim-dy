package com.edu.springboot.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;
import com.edu.springboot.infrastructure.persistence.memory.InMemoryAnomalySignalStore;
import com.edu.springboot.infrastructure.persistence.redis.RedisAnomalySignalStore;

@Configuration
public class AnomalySignalStoreConfig {

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "redis")
	AnomalySignalStore redisAnomalySignalStore(StringRedisTemplate redis) {
		return new RedisAnomalySignalStore(redis);
	}

	@Bean
	@ConditionalOnProperty(name = "app.phone-verify.store", havingValue = "memory", matchIfMissing = true)
	AnomalySignalStore inMemoryAnomalySignalStore() {
		return new InMemoryAnomalySignalStore();
	}
}
