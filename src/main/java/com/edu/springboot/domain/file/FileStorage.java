package com.edu.springboot.domain.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Hexaq
 * 계층: Domain
 * 객체: FileStorage
 * 책임: FileStorage 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface FileStorage {

	String store(String originalName, InputStream content) throws IOException;

	Path resolve(String storedName);
}
