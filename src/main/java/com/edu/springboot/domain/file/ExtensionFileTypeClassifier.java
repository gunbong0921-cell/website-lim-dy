package com.edu.springboot.domain.file;

import java.util.Locale;
import java.util.Set;

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
