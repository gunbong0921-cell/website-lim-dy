package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.domain.member.KakaoFriend;
import com.edu.springboot.domain.member.KakaoFriendRepository;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: KakaoFriendRepositoryImpl
 * 책임: 저장 포트 구현. JPA 또는 MyBatis
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Repository
@RequiredArgsConstructor
public class KakaoFriendRepositoryImpl implements KakaoFriendRepository {

	private final KakaoFriendJpaRepository jpa;

	@Override
	@Transactional
	public void replaceAll(Long memberId, List<KakaoFriend> friends) {
		jpa.deleteByMemberId(memberId);
		jpa.flush();
		if (friends != null && !friends.isEmpty()) {
			jpa.saveAll(friends);
		}
	}

	@Override
	public List<KakaoFriend> findByMemberId(Long memberId) {
		return jpa.findByMemberIdOrderByIdAsc(memberId);
	}
}
