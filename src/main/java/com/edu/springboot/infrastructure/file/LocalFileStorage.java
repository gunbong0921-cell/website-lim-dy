package com.edu.springboot.infrastructure.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.edu.springboot.domain.file.FileStorage;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: LocalFileStorage
 * 책임: LocalFileStorage 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
public class LocalFileStorage implements FileStorage {

	private final Path root;

	public LocalFileStorage(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
		this.root = Path.of(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(this.root);
	}

	@Override
	public String store(String originalName, InputStream content) throws IOException {
		String ext = "";
		if (originalName != null && originalName.contains(".")) {
			ext = originalName.substring(originalName.lastIndexOf('.'));
		}
		String stored = UUID.randomUUID().toString().replace("-", "") + ext;
		Files.copy(content, root.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
		return stored;
	}

	@Override
	public Path resolve(String storedName) {
		return root.resolve(storedName).normalize();
	}
}
