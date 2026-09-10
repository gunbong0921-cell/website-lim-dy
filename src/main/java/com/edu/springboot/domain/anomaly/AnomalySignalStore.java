package com.edu.springboot.domain.anomaly;

import java.time.Duration;
import java.util.OptionalLong;

public interface AnomalySignalStore {

	void rememberSignup(String loginId, long epochMilli, Duration ttl);

	OptionalLong signupAt(String loginId);

	boolean claimFirstCoreAction(String loginId, Duration ttl);

	long incrementWindow(String loginId, String apiName, Duration window);

	void pushGet(String loginId, String route, Duration lookback, int maxEntries);

	boolean hasGetSince(String loginId, long sinceEpochMilli);

	int incrementDirectCoreHits(String loginId, Duration ttl);

	void clearDirectCoreHits(String loginId);

	void markStep(String loginId, String step, Duration ttl);

	boolean hasStep(String loginId, String step);
}
