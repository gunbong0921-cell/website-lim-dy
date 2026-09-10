package com.edu.springboot.domain.mail;

/**
 * Hexaq
 * 계층: Domain
 * 객체: MailSender
 * 책임: MailSender 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/01-signup-verification.md](../../../../../../../../docs/features/01-signup-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface MailSender {

	void send(String to, String subject, String body);
}
