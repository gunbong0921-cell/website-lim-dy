package com.edu.springboot.infrastructure.captcha;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.edu.springboot.domain.captcha.CaptchaResult;
import com.sun.net.httpserver.HttpServer;

/**
 * Hexaq
 * 계층: Test
 * 객체: GoogleRecaptchaV3VerifierTest
 * 책임: siteverify 응답 파싱. 점수 판정 없음. Google 실호출 없음
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../../../../../../docs/security/03-recaptcha-v3.md)
 */
class GoogleRecaptchaV3VerifierTest {

	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	@DisplayName("비밀 키나 토큰이 비면 실패 결과다")
	void verify_failsWhenSecretOrTokenBlank() {
		GoogleRecaptchaV3Verifier verifier = new GoogleRecaptchaV3Verifier("http://127.0.0.1:1/siteverify", "");

		assertThat(verifier.verify("tok", "login", "1.1.1.1")).isEqualTo(CaptchaResult.failed());
		assertThat(new GoogleRecaptchaV3Verifier("http://127.0.0.1:1/siteverify", "secret").verify("  ", "login", null))
			.isEqualTo(CaptchaResult.failed());
	}

	@Test
	@DisplayName("네트워크 실패는 success=false다")
	void verify_failsOnUnreachableUrl() {
		GoogleRecaptchaV3Verifier verifier = new GoogleRecaptchaV3Verifier("http://127.0.0.1:1/siteverify", "secret");

		assertThat(verifier.verify("tok", "login", "1.1.1.1")).isEqualTo(CaptchaResult.failed());
	}

	@Test
	@DisplayName("Google JSON의 success·score·action만 옮긴다")
	void verify_mapsSiteverifyJsonWithoutJudgingScore() throws IOException {
		startServer("""
			{"success":true,"score":0.9,"action":"login"}
			""");
		GoogleRecaptchaV3Verifier verifier = new GoogleRecaptchaV3Verifier(verifyUrl(), "secret");

		assertThat(verifier.verify("tok", "login", "1.1.1.1"))
			.isEqualTo(new CaptchaResult(true, 0.9, "login"));
	}

	@Test
	@DisplayName("success가 false여도 점수는 판정하지 않고 그대로 둔다")
	void verify_doesNotApplyCutoff() throws IOException {
		startServer("""
			{"success":true,"score":0.2,"action":"phone_send_code"}
			""");
		GoogleRecaptchaV3Verifier verifier = new GoogleRecaptchaV3Verifier(verifyUrl(), "secret");

		assertThat(verifier.verify("tok", "phone_send_code", "1.1.1.1"))
			.isEqualTo(new CaptchaResult(true, 0.2, "phone_send_code"));
	}

	private void startServer(String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/siteverify", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, bytes.length);
			exchange.getResponseBody().write(bytes);
			exchange.close();
		});
		server.start();
	}

	private String verifyUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/siteverify";
	}
}
