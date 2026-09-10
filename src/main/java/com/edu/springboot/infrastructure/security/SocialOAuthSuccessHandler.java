package com.edu.springboot.infrastructure.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.SocialLoginService;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.application.member.dto.SocialProfile;
import com.edu.springboot.domain.member.AuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialOAuthSuccessHandler implements AuthenticationSuccessHandler {

	private final SocialLoginService socialLoginService;
	private final MemberSessionBinder memberSessionBinder;
	private final GitHubUserEmailClient gitHubUserEmailClient;
	private final KakaoFriendsClient kakaoFriendsClient;
	private final OAuth2AuthorizedClientService authorizedClientService;

	@Value("${app.oauth.success-redirect:/#/}")
	private String successRedirect;

	@Value("${app.oauth.failure-redirect:/#/login}")
	private String failureRedirect;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		AuthProvider provider = providerOf(authentication);
		try {
			SocialProfile profile = profileOf(authentication, provider);
			MemberResponse member = socialLoginService.loginOrSignUp(profile);
			memberSessionBinder.bind(request, response, new SessionPrincipal(member.loginId(), member.admin()));
			String target = member.needsPhone() ? "/#/mypage" : successRedirect;
			response.sendRedirect(target);
		} catch (BusinessException ex) {
			redirectFailure(response, ex.getMessage());
		} catch (Exception ex) {
			redirectFailure(response, provider.displayName() + " 로그인에 실패했습니다.");
		}
	}

	private AuthProvider providerOf(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken token) {
			return AuthProvider.from(token.getAuthorizedClientRegistrationId());
		}
		return AuthProvider.GOOGLE;
	}

	private SocialProfile profileOf(Authentication authentication, AuthProvider provider) {
		if (provider.kakao() && authentication instanceof OAuth2AuthenticationToken token) {
			return kakaoProfile(token);
		}
		if (provider.github() && authentication instanceof OAuth2AuthenticationToken token) {
			OAuth2User user = token.getPrincipal();
			String email = stringAttr(user, "email");
			if (email.isBlank()) {
				email = gitHubUserEmailClient.primaryEmail(token);
			}
			return new SocialProfile(
				"GITHUB",
				firstNonBlank(stringAttr(user, "id"), user.getName()),
				email,
				firstNonBlank(stringAttr(user, "name"), stringAttr(user, "login"))
			);
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof OidcUser oidc) {
			return new SocialProfile("GOOGLE", oidc.getSubject(), oidc.getEmail(), oidc.getFullName());
		}
		if (principal instanceof OAuth2User user) {
			return new SocialProfile(
				provider.name(),
				user.getName(),
				stringAttr(user, "email"),
				firstNonBlank(stringAttr(user, "name"), stringAttr(user, "given_name"))
			);
		}
		throw new BusinessException("소셜 계정 정보를 읽을 수 없습니다.");
	}

	private SocialProfile kakaoProfile(OAuth2AuthenticationToken token) {
		OAuth2User user = token.getPrincipal();
		String id = firstNonBlank(stringAttr(user, "id"), user.getName());
		String email = "";
		String name = "";
		String profileImage = "";
		Object account = user.getAttribute("kakao_account");
		if (account instanceof java.util.Map<?, ?> map) {
			Object accountEmail = map.get("email");
			if (accountEmail != null) {
				email = String.valueOf(accountEmail);
			}
			Object profile = map.get("profile");
			if (profile instanceof java.util.Map<?, ?> profileMap) {
				name = firstNonBlank(text(profileMap.get("nickname")), name);
				profileImage = firstNonBlank(
					text(profileMap.get("profile_image_url")),
					text(profileMap.get("thumbnail_image_url"))
				);
			}
		}
		if (name.isBlank() || profileImage.isBlank()) {
			Object properties = user.getAttribute("properties");
			if (properties instanceof java.util.Map<?, ?> props) {
				if (name.isBlank()) {
					name = text(props.get("nickname"));
				}
				if (profileImage.isBlank()) {
					profileImage = firstNonBlank(text(props.get("profile_image")), text(props.get("thumbnail_image")));
				}
			}
		}
		if (name.isBlank()) {
			name = "카카오회원";
		}
		if (email.isBlank() && !id.isBlank()) {
			email = "kakao-" + id + "@kakao.hexaq.local";
		}
		return new SocialProfile("KAKAO", id, email, name, profileImage, kakaoFriendsClient.list(token),
			accessToken(token));
	}

	private String accessToken(OAuth2AuthenticationToken token) {
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			token.getAuthorizedClientRegistrationId(),
			token.getName()
		);
		if (client == null || client.getAccessToken() == null) {
			return "";
		}
		return client.getAccessToken().getTokenValue();
	}

	private String text(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	private void redirectFailure(HttpServletResponse response, String message) throws IOException {
		String encoded = URLEncoder.encode(message == null ? "소셜 로그인에 실패했습니다." : message, StandardCharsets.UTF_8);
		response.sendRedirect(failureRedirect + "?oauthError=" + encoded);
	}

	private String stringAttr(OAuth2User user, String key) {
		Object value = user.getAttribute(key);
		return value == null ? "" : String.valueOf(value);
	}

	private String firstNonBlank(String left, String right) {
		if (left != null && !left.isBlank()) {
			return left;
		}
		return right == null ? "" : right;
	}
}
