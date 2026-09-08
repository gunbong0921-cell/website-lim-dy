package com.edu.springboot.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.springboot.domain.member.AuthProvider;
import com.edu.springboot.domain.member.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByLoginId(String loginId);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);
}
