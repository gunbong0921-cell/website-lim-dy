package com.edu.springboot.domain.member;

import java.util.List;

public interface KakaoFriendRepository {

	void replaceAll(Long memberId, List<KakaoFriend> friends);

	List<KakaoFriend> findByMemberId(Long memberId);
}
