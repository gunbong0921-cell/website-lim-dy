package com.edu.springboot.domain.member;

import java.util.List;

/**
 * Hexaq
 * 계층: Domain
 * 객체: KakaoFriendRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface KakaoFriendRepository {

	void replaceAll(Long memberId, List<KakaoFriend> friends);

	List<KakaoFriend> findByMemberId(Long memberId);
}
