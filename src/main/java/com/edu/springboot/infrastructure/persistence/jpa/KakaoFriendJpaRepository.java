package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.springboot.domain.member.KakaoFriend;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: KakaoFriendJpaRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public interface KakaoFriendJpaRepository extends JpaRepository<KakaoFriend, Long> {

	void deleteByMemberId(Long memberId);

	List<KakaoFriend> findByMemberIdOrderByIdAsc(Long memberId);
}
