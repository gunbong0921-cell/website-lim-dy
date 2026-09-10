package com.edu.springboot.domain.captcha;

/**
 * Hexaq
 * 계층: Domain
 * 객체: CaptchaPolicy
 * 책임: 점수·action 판정. *.trycloudflare.com 은 requiredOnHost=false
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../docs/security/03-recaptcha-v3.md) · [docs/technical/03-cloudflare-tunnel.md](../../../../../../../../docs/technical/03-cloudflare-tunnel.md)
 */
public class CaptchaPolicy {

	public boolean passed(CaptchaResult result, String expectedAction, double minScore) {
		if (result == null || !result.success()) {
			return false;
		}
		if (expectedAction == null || expectedAction.isBlank()) {
			return false;
		}
		if (!expectedAction.equals(result.action())) {
			return false;
		}
		return result.score() >= minScore;
	}

	public boolean requiredOnHost(String host) {
		String hostname = hostname(host);
		return !hostname.endsWith(".trycloudflare.com");
	}

	private static String hostname(String host) {
		if (host == null || host.isBlank()) {
			return "";
		}
		String value = host.trim().toLowerCase();
		int slash = value.indexOf('/');
		if (slash >= 0) {
			value = value.substring(0, slash);
		}
		int colon = value.indexOf(':');
		if (colon > 0) {
			value = value.substring(0, colon);
		}
		return value;
	}
}
