package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.springboot.domain.member.AuthProvider;
import com.edu.springboot.domain.member.Member;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: MemberJpaRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public interface MemberJpaRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByLoginId(String loginId);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);
}
