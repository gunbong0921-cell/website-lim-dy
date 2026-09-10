package com.edu.springboot.application.member.dto;

/**
 * Hexaq
 * 계층: Application
 * 객체: BusinessVerifyResponse
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
public record BusinessVerifyResponse(
	boolean matched,
	boolean operating,
	String statusName,
	String taxType
) {
}
