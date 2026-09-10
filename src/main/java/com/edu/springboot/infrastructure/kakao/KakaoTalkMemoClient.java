package com.edu.springboot.infrastructure.kakao;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.edu.springboot.domain.member.KakaoTalkGateway;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: KakaoTalkMemoClient
 * 책임: KakaoTalkMemoClient 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/04-social-login.md](../../../../../../../../docs/features/04-social-login.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
public class KakaoTalkMemoClient implements KakaoTalkGateway {

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private final RestClient restClient = RestClient.builder().build();
	private final String guideUrl;

	public KakaoTalkMemoClient(
		@Value("${app.kakao.guide-url:http://localhost:8282/#/insights}") String guideUrl
	) {
		this.guideUrl = guideUrl;
	}

	@Override
	public boolean sendWelcome(String accessToken, String memberName) {
		if (accessToken == null || accessToken.isBlank()) {
			return false;
		}
		String name = memberName == null || memberName.isBlank() ? "회원" : memberName.trim();
		String text = """
			[Hexaq] Welcome to Hexaq!

			안녕하세요, %s님.

			Hexaq의 파트너가 되신 것을 환영합니다!

			신규 회원분들을 위해 준비된 Hexaq 시작 가이드와 주요 인프라 혜택을 확인해 보세요.
			""".formatted(name).strip();
		String template = """
			{"object_type":"text","text":"%s","link":{"web_url":"%s","mobile_web_url":"%s"},"button_title":"시작 가이드 확인하기"}
			""".formatted(escapeJson(text), escapeJson(guideUrl), escapeJson(guideUrl)).strip();
		try {
			Map<String, Object> body = restClient.post()
				.uri("https://kapi.kakao.com/v2/api/talk/memo/default/send")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body("template_object=" + URLEncoder.encode(template, StandardCharsets.UTF_8))
				.retrieve()
				.body(MAP_TYPE);
			if (body == null) {
				return false;
			}
			Object code = body.get("result_code");
			return code instanceof Number number && number.intValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\r", "\\r")
			.replace("\n", "\\n");
	}
}
