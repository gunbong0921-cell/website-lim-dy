package com.edu.springboot.domain.member;

import java.util.Optional;

/**
 * Hexaq
 * 계층: Domain
 * 객체: MemberRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface MemberRepository {

	Member save(Member member);

	Optional<Member> findById(Long id);

	Optional<Member> findByLoginId(String loginId);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);
}
