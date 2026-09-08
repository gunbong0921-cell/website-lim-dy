package com.edu.springboot.domain.file;

public interface FileTypeClassifier {

	AttachmentType classify(String filename);
}
