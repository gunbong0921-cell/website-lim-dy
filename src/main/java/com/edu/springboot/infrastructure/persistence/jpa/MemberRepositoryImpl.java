package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.edu.springboot.domain.member.AuthProvider;
import com.edu.springboot.domain.member.Member;
import com.edu.springboot.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;

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
