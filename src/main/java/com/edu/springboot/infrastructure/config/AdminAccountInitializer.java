package com.edu.springboot.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.edu.springboot.application.member.EnsureAdminMemberService;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: AdminAccountInitializer
 * 책임: AdminAccountInitializer 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md) · [docs/technical/04-redis.md](../../../../../../../../docs/technical/04-redis.md)
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

	private final EnsureAdminMemberService ensureAdminMemberService;
	private final String loginId;
	private final String password;
	private final String name;
	private final String email;
	private final String phone;

	public AdminAccountInitializer(
		EnsureAdminMemberService ensureAdminMemberService,
		@Value("${app.admin.login-id:}") String loginId,
		@Value("${app.admin.password:}") String password,
		@Value("${app.admin.name:관리자}") String name,
		@Value("${app.admin.email:}") String email,
		@Value("${app.admin.phone:010-0000-0000}") String phone
	) {
		this.ensureAdminMemberService = ensureAdminMemberService;
		this.loginId = loginId;
		this.password = password;
		this.name = name;
		this.email = email;
		this.phone = phone;
	}

	@Override
	public void run(ApplicationArguments args) {
		ensureAdminMemberService.ensure(loginId, password, name, email, phone);
	}
}
