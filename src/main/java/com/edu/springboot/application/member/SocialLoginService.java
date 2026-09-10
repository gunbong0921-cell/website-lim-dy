package com.edu.springboot.application.member;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.anomaly.RememberSignupService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.MemberMapper;
import com.edu.springboot.application.member.dto.MemberResponse;
import com.edu.springboot.application.member.dto.SocialProfile;
import com.edu.springboot.domain.member.AuthProvider;
import com.edu.springboot.domain.member.KakaoFriend;
import com.edu.springboot.domain.member.KakaoFriendRepository;
import com.edu.springboot.domain.member.KakaoTalkGateway;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;
import com.edu.springboot.domain.member.MemberType;
import com.edu.springboot.domain.member.PasswordEncryptor;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: SocialLoginService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginService {

	private final MemberRepository memberRepository;
	private final KakaoFriendRepository kakaoFriendRepository;
	private final KakaoTalkGateway kakaoTalkGateway;
	private final PasswordEncryptor passwordEncryptor;
	private final RememberSignupService rememberSignupService;

	public MemberResponse loginOrSignUp(SocialProfile profile) {
		AuthProvider provider = AuthProvider.from(profile.provider());
		if (!provider.social()) {
			throw new BusinessException("지원하지 않는 소셜 로그인입니다.");
		}
		String subject = trim(profile.subject());
		String email = trim(profile.email()).toLowerCase();
		String name = trim(profile.name());
		if (subject.isEmpty()) {
			throw new BusinessException(provider.displayName() + " 계정 식별자를 확인할 수 없습니다.");
		}
		if (email.isEmpty() || !email.contains("@")) {
			throw new BusinessException(provider.displayName() + " 계정에서 이메일을 확인할 수 없습니다.");
		}
		Member linked = memberRepository.findByOauthProviderAndOauthId(provider, subject).orElse(null);
		if (linked != null) {
			return finish(linked, provider, profile, false);
		}
		Member byEmail = memberRepository.findByEmail(email).orElse(null);
		if (byEmail != null) {
			if (byEmail.memberType().corporate()) {
				throw new BusinessException("기업 회원은 소셜 로그인을 사용할 수 없습니다.");
			}
			if (byEmail.socialLinked() && byEmail.oauthProvider() != provider) {
				throw new BusinessException("이미 " + byEmail.oauthProvider().displayName()
					+ " 계정과 연결된 이메일입니다. 해당 소셜 로그인을 이용해 주세요.");
			}
			if (byEmail.socialLinked() && !subject.equals(byEmail.getOauthId())) {
				throw new BusinessException("이미 다른 " + provider.displayName() + " 계정과 연결되어 있습니다.");
			}
			byEmail.linkOauth(provider, subject);
			return finish(byEmail, provider, profile, false);
		}
		return finish(createIndividual(provider, subject, email, name), provider, profile, true);
	}

	private Member createIndividual(AuthProvider provider, String subject, String email, String name) {
		Member member = new Member();
		member.setLoginId(uniqueLoginId(provider, email));
		member.setPassword(passwordEncryptor.encode(UUID.randomUUID().toString()));
		member.setName(name.isEmpty() ? email.substring(0, email.indexOf('@')) : name);
		member.setEmail(email);
		member.setPhone("-");
		member.assignType(MemberType.INDIVIDUAL);
		member.agreeTerms(true, true, false, false);
		member.linkOauth(provider, subject);
		return memberRepository.save(member);
	}

	private MemberResponse finish(Member member, AuthProvider provider, SocialProfile profile, boolean newMember) {
		if (member.memberType().corporate()) {
			throw new BusinessException("기업 회원은 " + provider.displayName() + " 로그인을 사용할 수 없습니다.");
		}
		member.markEmailVerified();
		member.applySocialProfile(trim(profile.name()), trim(profile.profileImage()));
		Member saved = memberRepository.save(member);
		if (newMember) {
			rememberSignupService.remember(saved.getLoginId());
		}
		if (provider.kakao()) {
			kakaoFriendRepository.replaceAll(saved.getId(), toFriends(saved.getId(), profile.friends()));
			if (newMember && !saved.kakaoWelcomeSent()) {
				boolean sent = kakaoTalkGateway.sendWelcome(profile.accessToken(), saved.getName());
				saved.recordKakaoWelcome(sent);
				saved = memberRepository.save(saved);
			}
		}
		return MemberMapper.toResponse(saved, kakaoFriendRepository.findByMemberId(saved.getId()));
	}

	private List<KakaoFriend> toFriends(Long memberId, List<SocialProfile.SocialFriend> friends) {
		if (friends == null || friends.isEmpty()) {
			return List.of();
		}
		return friends.stream()
			.filter(friend -> friend.id() != null && !friend.id().isBlank())
			.map(friend -> KakaoFriend.of(
				memberId,
				friend.id(),
				friend.nickname(),
				friend.profileImage(),
				friend.favorite()
			))
			.toList();
	}

	private String uniqueLoginId(AuthProvider provider, String email) {
		if (!memberRepository.existsByLoginId(email)) {
			return email;
		}
		String tag = provider.github() ? "github" : provider.name().toLowerCase();
		String base = email.replace("@", "+" + tag + "@");
		if (!memberRepository.existsByLoginId(base)) {
			return base;
		}
		return email + "." + UUID.randomUUID().toString().substring(0, 8);
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}
}
