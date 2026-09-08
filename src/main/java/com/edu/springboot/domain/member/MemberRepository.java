package com.edu.springboot.domain.member;

import java.util.Optional;

public interface MemberRepository {

	Member save(Member member);

	Optional<Member> findById(Long id);

	Optional<Member> findByLoginId(String loginId);

	Optional<Member> findByEmail(String email);

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);
}
