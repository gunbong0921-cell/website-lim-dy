package com.edu.springboot.domain.file;

import java.util.Locale;
import java.util.Set;

/**
 * Hexaq
 * 계층: Domain
 * 객체: ExtensionFileTypeClassifier
 * 책임: ExtensionFileTypeClassifier 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class ExtensionFileTypeClassifier implements FileTypeClassifier {

	private static final Set<String> IMAGE = Set.of("png", "gif", "jpg", "jpeg", "webp", "bmp");
	private static final Set<String> VIDEO = Set.of("mp4", "webm", "ogg", "avi");
	private static final Set<String> AUDIO = Set.of("mp3", "wav", "aac", "m4a");

	@Override
	public AttachmentType classify(String filename) {
		if (filename == null || !filename.contains(".")) {
			return AttachmentType.DOWNLOAD;
		}
		String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		if (IMAGE.contains(ext)) {
			return AttachmentType.IMAGE;
		}
		if (VIDEO.contains(ext)) {
			return AttachmentType.VIDEO;
		}
		if (AUDIO.contains(ext)) {
			return AttachmentType.AUDIO;
		}
		return AttachmentType.DOWNLOAD;
	}
}
