package com.edu.springboot.infrastructure.security;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GitHubUserEmailClient {

	private static final ParameterizedTypeReference<List<Map<String, Object>>> EMAILS_TYPE =
		new ParameterizedTypeReference<>() {
		};

	private final OAuth2AuthorizedClientService authorizedClientService;
	private final RestClient restClient = RestClient.builder().build();

	public String primaryEmail(OAuth2AuthenticationToken token) {
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			token.getAuthorizedClientRegistrationId(),
			token.getName()
		);
		if (client == null || client.getAccessToken() == null) {
			return "";
		}
		try {
			List<Map<String, Object>> emails = restClient.get()
				.uri("https://api.github.com/user/emails")
				.header("Authorization", "Bearer " + client.getAccessToken().getTokenValue())
				.header("Accept", "application/vnd.github+json")
				.retrieve()
				.body(EMAILS_TYPE);
			if (emails == null || emails.isEmpty()) {
				return "";
			}
			String primaryVerified = pick(emails, true, true);
			if (!primaryVerified.isEmpty()) {
				return primaryVerified;
			}
			String verified = pick(emails, false, true);
			if (!verified.isEmpty()) {
				return verified;
			}
			Object first = emails.get(0).get("email");
			return first == null ? "" : String.valueOf(first);
		} catch (Exception ex) {
			return "";
		}
	}

	private String pick(List<Map<String, Object>> emails, boolean primaryOnly, boolean verifiedOnly) {
		for (Map<String, Object> row : emails) {
			boolean primary = Boolean.TRUE.equals(row.get("primary"));
			boolean verified = Boolean.TRUE.equals(row.get("verified"));
			if (primaryOnly && !primary) {
				continue;
			}
			if (verifiedOnly && !verified) {
				continue;
			}
			Object email = row.get("email");
			if (email != null && !String.valueOf(email).isBlank()) {
				return String.valueOf(email);
			}
		}
		return "";
	}
}
