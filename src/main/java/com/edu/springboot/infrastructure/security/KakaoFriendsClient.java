package com.edu.springboot.infrastructure.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.edu.springboot.application.member.dto.SocialProfile.SocialFriend;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoFriendsClient {

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private final OAuth2AuthorizedClientService authorizedClientService;
	private final RestClient restClient = RestClient.builder().build();

	public List<SocialFriend> list(OAuth2AuthenticationToken token) {
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			token.getAuthorizedClientRegistrationId(),
			token.getName()
		);
		if (client == null || client.getAccessToken() == null) {
			return List.of();
		}
		try {
			Map<String, Object> body = restClient.get()
				.uri("https://kapi.kakao.com/v1/api/talk/friends")
				.header("Authorization", "Bearer " + client.getAccessToken().getTokenValue())
				.retrieve()
				.body(MAP_TYPE);
			if (body == null) {
				return List.of();
			}
			Object elements = body.get("elements");
			if (!(elements instanceof List<?> list)) {
				return List.of();
			}
			List<SocialFriend> friends = new ArrayList<>();
			for (Object row : list) {
				if (!(row instanceof Map<?, ?> map)) {
					continue;
				}
				Object id = map.get("id");
				if (id == null) {
					continue;
				}
				friends.add(new SocialFriend(
					String.valueOf(id),
					text(map.get("profile_nickname")),
					text(map.get("profile_thumbnail_image")),
					Boolean.TRUE.equals(map.get("favorite"))
				));
			}
			return friends;
		} catch (Exception ex) {
			return List.of();
		}
	}

	private String text(Object value) {
		return value == null ? "" : String.valueOf(value);
	}
}
