package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.member.AuthProvider;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: MemberRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository jpa;

	@Override
	public Member save(Member member) {
		return jpa.save(member);
	}

	@Override
	public Optional<Member> findById(Long id) {
		return jpa.findById(id);
	}

	@Override
	public Optional<Member> findByLoginId(String loginId) {
		return jpa.findByLoginId(loginId);
	}

	@Override
	public Optional<Member> findByEmail(String email) {
		return jpa.findByEmail(email);
	}

	@Override
	public Optional<Member> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId) {
		return jpa.findByOauthProviderAndOauthId(oauthProvider, oauthId);
	}

	@Override
	public boolean existsByLoginId(String loginId) {
		return jpa.existsByLoginId(loginId);
	}

	@Override
	public boolean existsByEmail(String email) {
		return jpa.existsByEmail(email);
	}
}
