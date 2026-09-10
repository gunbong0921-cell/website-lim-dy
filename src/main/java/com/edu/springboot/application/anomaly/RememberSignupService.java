package com.edu.springboot.application.anomaly;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edu.springboot.domain.anomaly.AnomalySignalStore;

@Service
public class RememberSignupService {

	private final AnomalySignalStore anomalySignalStore;
	private final Duration ttl;

	public RememberSignupService(
		AnomalySignalStore anomalySignalStore,
		@Value("${app.anomaly.signup-ttl-hours:24}") int signupTtlHours
	) {
		this.anomalySignalStore = anomalySignalStore;
		this.ttl = Duration.ofHours(Math.max(1, signupTtlHours));
	}

	public void remember(String loginId) {
		if (loginId == null || loginId.isBlank()) {
			return;
		}
		anomalySignalStore.rememberSignup(loginId.trim(), System.currentTimeMillis(), ttl);
	}
}
