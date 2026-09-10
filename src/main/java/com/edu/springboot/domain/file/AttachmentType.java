package com.edu.springboot.domain.file;

/**
 * Hexaq
 * 계층: Domain
 * 객체: AttachmentType
 * 책임: AttachmentType 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public enum AttachmentType {
	IMAGE,
	VIDEO,
	AUDIO,
	DOWNLOAD
}
