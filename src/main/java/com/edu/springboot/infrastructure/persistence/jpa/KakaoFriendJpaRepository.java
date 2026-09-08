package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.springboot.domain.member.KakaoFriend;

public interface KakaoFriendJpaRepository extends JpaRepository<KakaoFriend, Long> {

	void deleteByMemberId(Long memberId);

	List<KakaoFriend> findByMemberIdOrderByIdAsc(Long memberId);
}
