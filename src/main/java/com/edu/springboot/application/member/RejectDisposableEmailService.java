package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.member.DisposableEmailCatalog;
import com.edu.springboot.domain.member.EmailPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: RejectDisposableEmailService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/features/02-business-registration.md](../../../../../../../../docs/features/02-business-registration.md) · [docs/features/03-login-logout.md](../../../../../../../../docs/features/03-login-logout.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
public class RejectDisposableEmailService {

	private final EmailPolicy emailPolicy;
	private final DisposableEmailCatalog disposableEmailCatalog;

	public String requireAllowed(String rawEmail) {
		String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
		if (!emailPolicy.validFormat(email)) {
			throw new BusinessException("이메일 형식이 올바르지 않습니다.");
		}
		if (disposableEmailCatalog.blocked(emailPolicy.domainOf(email))) {
			throw new BusinessException("일회용 이메일은 사용할 수 없습니다.");
		}
		return email;
	}
}
