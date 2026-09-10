package com.edu.springboot.domain.sms;

/**
 * Hexaq
 * 계층: Domain
 * 객체: SmsSender
 * 책임: SmsSender 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/06-identity-verification.md](../../../../../../../../docs/security/06-identity-verification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface SmsSender {

	void send(String to, String text);
}
